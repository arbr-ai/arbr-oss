package com.arbr.engine.services.embedding.dal

import com.arbr.content_formats.jsonb.serializeToJsonb
import com.arbr.engine.services.db.client.EmbeddedResourcePairStore
import com.arbr.engine.services.embedding.model.EmbeddedResourcePairCreationParameters
import com.arbr.engine.services.vector_db.model.Namespace
import com.arbr.engine.services.vector_db.model.VectorDbEntryMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import com.arbr.relational_prompting.services.embedding.model.TemplateElementLiteral
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import com.arbr.db.public.tables.records.IndexedResourceRecord
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class EmbeddedResourcePairDALImpl(
    private val coreEmbeddingDAL: CoreEmbeddingDAL,
    private val embeddedResourcePairStore: EmbeddedResourcePairStore,
    private val mapper: ObjectMapper,
) : EmbeddedResourcePairDAL, ApplicationExampleProvider {
    private val genericSourcedValueList = jacksonTypeRef<List<SourcedValue<*>>>()

    /**
     * Publish new entries and return all pairs, not necessarily in order.
     */
    private fun publish(
        creationParametersFlux: Flux<EmbeddedResourcePairCreationParameters>,
        namespace: Namespace,
    ): Flux<EmbeddedResourcePair> {
        return creationParametersFlux.flatMap { creationParameters ->
            coreEmbeddingDAL.publishAll(
                creationParameters.inputEmbeddingContents,
                fixedExampleResourceVersion,
                namespace,
            ).collectList()
                .flatMapMany { insertedCoreEmbeddings ->
                    val vectorIds = insertedCoreEmbeddings.map { it.coreEmbedding.vectorId }
                    embeddedResourcePairStore.insert(vectorIds, creationParameters)
                }
        }
    }

    override fun retrieveNearestNeighbors(
        embeddingContents: List<ResourceEmbeddingContent>,
        namespace: Namespace,
        numNeighbors: Long,
    ): Flux<EmbeddedResourcePair> {
        return Flux.fromIterable(embeddingContents).flatMap { resourceEmbeddingContent ->
            coreEmbeddingDAL.retrieveNearestNeighbors(
                resourceEmbeddingContent,
                fixedExampleResourceVersion,
                namespace,
                numNeighbors,
            ).map { resourceEmbeddingContent.kind to it }
        }
            .collectList()
            .map { kindRespList ->
                val numKinds = kindRespList.size

                val scoreLists = mutableMapOf<String, MutableList<Double>>()
                for ((i, pair) in kindRespList.withIndex()) {
                    // TODO: Allow providing weights for embedding kinds
                    val (_, result) = pair
                    if (result.vectorId !in scoreLists) {
                        scoreLists[result.vectorId] = (0 until numKinds).map { 0.0 }.toMutableList()
                    }
                    scoreLists[result.vectorId]!![i] = 1 - result.distance
                }

                scoreLists.toList().sortedByDescending {
                    it.second.sum()
                }
                    .map { it.first }
                    .take(numNeighbors.toInt())
            }
            .flatMapIterable { it }
            .concatMap { vectorId ->
                // TODO: Batch
                // TODO: Warn for values present in vector DB but not local storage
                embeddedResourcePairStore.get(vectorId)
                    .next()
            }
    }

    /**
     * Get an embedded resource pair by its vector ID.
     * Assumes it exists in the vector DB already.
     */
    override fun getEmbeddedResourcePairByVectorId(
        vectorId: String,
    ): Mono<EmbeddedResourcePair> {
        return embeddedResourcePairStore.get(vectorId)
            .next()
    }

    /**
     * Update content associated with the given ID.
     */
    override fun updateEmbeddedResourcePair(
        vectorId: String,
        namespace: Namespace,
        newChatMessages: List<ChatMessage>,
        newTier: Long,
    ): Mono<Void> {
        return embeddedResourcePairStore.get(vectorId).flatMap { entry ->
            val newMetadata = entry.metadata!!.let { mdJsonb ->
                val md = mapper.readValue(mdJsonb.data(), VectorDbEntryMetadata::class.java)
                md["tier"] = newTier
                serializeToJsonb(mapper, md)
            }
            val newChatMessagesJsonb = serializeToJsonb(mapper, newChatMessages)

            embeddedResourcePairStore.update(
                vectorId,
                newChatMessagesJsonb,
                newMetadata,
            )
                .then()
        }.then()
    }

    /**
     * Delete an embedded resource pair by vector ID.
     */
    override fun deleteEmbeddedResourcePair(
        vectorId: String,
        namespace: Namespace,
    ): Mono<Void> {
        return embeddedResourcePairStore.delete(vectorId)
            .then()
    }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> publishAll(
        pairs: List<Pair<TemplateElementLiteral<InputModel>, TemplateElementLiteral<OutputModel>>>
    ): Flux<EmbeddedResourcePair> {
        val nowMs = Instant.now().toEpochMilli()

        val entries = pairs.map { (inputLiteral, outputLiteral) ->
            val inputSchema = inputLiteral.schema
            val outputSchema = outputLiteral.schema
            val inputMessages = inputLiteral.chatMessages
            val outputMessages = outputLiteral.chatMessages
            val input = inputLiteral.obj
            val output = outputLiteral.obj

            val inputResource = IndexedResourceRecord(
                id = null,
                creationTimestamp = nowMs,
                schemaId = input.getSchemaId(),
                resourceObject = serializeToJsonb(mapper, inputSchema.serializedToSourcedValues(input)),
                chatMessages = serializeToJsonb(mapper, inputMessages),
            )
            val outputResource = IndexedResourceRecord(
                id = null,
                creationTimestamp = nowMs,
                schemaId = input.getSchemaId(),
                resourceObject = serializeToJsonb(mapper, outputSchema.serializedToSourcedValues(output)),
                chatMessages = serializeToJsonb(mapper, outputMessages),
            )

            EmbeddedResourcePairCreationParameters(
                inputResource,
                outputResource,
                inputSchema.serializedEmbeddingContents(input),
            )
        }

        return publish(
            Flux.fromIterable(entries),
            Namespace.AI_APPLICATION_EXAMPLE,
        )
    }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> retrieveNearestNeighbors(
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        input: InputModel,
        numNeighborsToRetrieve: Long
    ): Flux<VectorResourceKeyValuePair<InputModel, OutputModel>> {
        return if (numNeighborsToRetrieve == 0L) {
            Flux.empty()
        } else {
            retrieveNearestNeighbors(
                inputSchema.serializedEmbeddingContents(input),
                Namespace.AI_APPLICATION_EXAMPLE,
                numNeighborsToRetrieve,
            ).mapNotNull { kvEntry ->
                try {
                    val inputProxyObject = mapper.readValue(kvEntry.inputResourceObject!!.data(), genericSourcedValueList)
                    val outputProxyObject = mapper.readValue(kvEntry.outputResourceObject!!.data(), genericSourcedValueList)

                    val inputValue = try {
                        inputSchema.deserializeFromSourcedValues(inputProxyObject)
                    } catch (e: Exception) {
                        logger.warn("Found example but input value was uninterpretable", e)
                        return@mapNotNull null
                    }
                    val outputValue = try {
                        outputSchema.deserializeFromSourcedValues(outputProxyObject)
                    } catch (e: Exception) {
                        logger.warn("Found example but output value was uninterpretable", e)
                        return@mapNotNull null
                    }

                    try {
                        mapper.writeValueAsString(inputValue)
                    } catch (e: Exception) {
                        logger.warn("Unusable input value from example", e)
                        return@mapNotNull null
                    }

                    try {
                        mapper.writeValueAsString(outputValue)
                    } catch (e: Exception) {
                        logger.warn("Unusable output value from example", e)
                        return@mapNotNull null
                    }

                    val inputFqi = TemplateElementLiteral(
                        inputSchema,
                        inputValue,
                        mapper.readValue(kvEntry.inputChatMessages!!.data(), jacksonTypeRef()),
                    )
                    val outputFqi = TemplateElementLiteral(
                        outputSchema,
                        outputValue,
                        mapper.readValue(kvEntry.outputChatMessages!!.data(), jacksonTypeRef()),
                    )

                    VectorResourceKeyValuePair(
                        kvEntry.vectorId!!,
                        inputFqi,
                        outputFqi,
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to parse embedded KV entry, ignoring")
                    null
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmbeddedResourcePairDAL::class.java)

        private const val fixedExampleResourceVersion = "0"
    }
}