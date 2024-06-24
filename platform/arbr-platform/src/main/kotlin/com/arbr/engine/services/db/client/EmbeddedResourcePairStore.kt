package com.arbr.engine.services.db.client

import com.arbr.engine.services.embedding.model.EmbeddedResourcePairCreationParameters
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import com.arbr.db.public.tables.records.EmbeddedContentRecord
import com.arbr.db.public.tables.records.IndexedResourceRecord
import com.arbr.db.public.tables.records.IoPairRecord
import com.arbr.db.public.tables.references.EMBEDDED_RESOURCE_PAIR
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.value
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Instant

@Component
class EmbeddedResourcePairStore(
    private val indexedResourceStore: IndexedResourceStore,
    private val ioPairStore: IoPairStore,
    private val embeddedContentStore: EmbeddedContentStore,
    private val dslContext: DSLContext,
) {
    fun insert(
        vectorIds: List<String>,
        params: EmbeddedResourcePairCreationParameters
    ): Flux<EmbeddedResourcePair> {
        val input = params.inputRecord
        val output = params.outputRecord

        return Mono.zip(
            indexedResourceStore.insert(input),
            indexedResourceStore.insert(output),
        ).flatMap { (inputResource, outputResource) ->
            val creationTimestamp = Instant.now().toEpochMilli()

            Mono.zip(
                ioPairStore.insert(
                    IoPairRecord(
                        id = null,
                        creationTimestamp = creationTimestamp,
                        inputResourceId = inputResource.id!!,
                        outputResourceId = outputResource.id!!,
                    )
                ),
                Flux.fromIterable(vectorIds.zip(params.inputEmbeddingContents)).flatMap { (vectorId, inputEmbeddingContent) ->
                    if (inputEmbeddingContent.content.length > MAX_CONTENT_LENGTH) {
                        logger.warn("Embedding content too long to write to DB at ${inputEmbeddingContent.content.length}")
                        Mono.empty()
                    } else {
                        val embeddedContentRecord = EmbeddedContentRecord(
                            id = null,
                            creationTimestamp = creationTimestamp,
                            resourceId = inputResource.id!!,
                            vectorId = vectorId,
                            schemaId = input.schemaId,
                            kind = inputEmbeddingContent.kind.key,
                            embeddingContent = inputEmbeddingContent.content,
                            metadata = null,
                        )
                        embeddedContentStore.insert(
                            embeddedContentRecord
                        )
                    }
                }
                    .map { embeddedContent ->
                        EmbeddedResourcePair(
                            inputResourceId = inputResource.id,
                            inputCreationTimestamp = inputResource.creationTimestamp,
                            inputSchemaId = inputResource.schemaId,
                            inputResourceObject = inputResource.resourceObject,
                            inputChatMessages = inputResource.chatMessages,
                            vectorId = embeddedContent.vectorId,
                            embeddingContent = embeddedContent.embeddingContent,
                            metadata = embeddedContent.metadata,
                            outputResourceId = outputResource.id,
                            outputCreationTimestamp = output.creationTimestamp,
                            outputSchemaId = output.schemaId,
                            outputResourceObject = output.resourceObject,
                            outputChatMessages = output.chatMessages,
                        )
                    }
                    .collectList()
            )
        }.flatMapIterable { it.t2 }
    }

    fun get(
        vectorId: String,
    ): Flux<EmbeddedResourcePair> {
        return Flux.from(
            dslContext
                .select(asterisk())
                .from(EMBEDDED_RESOURCE_PAIR)
                .where(EMBEDDED_RESOURCE_PAIR.VECTOR_ID.eq(value(vectorId)))
        ).map { record ->
            record.into(EmbeddedResourcePair::class.java)
        }
    }

    fun update(
        vectorId: String,
        chatMessages: JSONB,
        metadata: JSONB
    ): Mono<Void> {
        return get(vectorId).flatMap { embeddedResourcePair ->
            val newIndexedResourceRecord = IndexedResourceRecord(
                embeddedResourcePair.inputResourceId,
                embeddedResourcePair.inputCreationTimestamp!!,
                embeddedResourcePair.inputSchemaId!!,
                embeddedResourcePair.inputResourceObject!!,
                chatMessages,
            )

            indexedResourceStore.update(newIndexedResourceRecord).thenMany(
                embeddedContentStore.getByInputResourceId(embeddedResourcePair.inputResourceId!!)
                    .flatMap { embeddedContent ->
                        val newEmbeddedContentRecord = EmbeddedContentRecord(
                            embeddedContent.id,
                            embeddedContent.creationTimestamp,
                            embeddedContent.resourceId,
                            embeddedContent.vectorId,
                            embeddedContent.schemaId,
                            kind = "",
                            embeddedContent.embeddingContent,
                            metadata,
                        )

                        embeddedContentStore.update(
                            newEmbeddedContentRecord
                        )
                    }
            )
        }.then()
    }

    fun delete(vectorId: String): Mono<Void> {
        // Delete embedded content, then io pairs for which the vector id corresponds to an input, then delete
        // input and output indexed resources.
        return get(vectorId).flatMap { embeddedResourcePair ->
            val inputIndexedResourceId = embeddedResourcePair.inputResourceId!!
            Mono.zip(
                embeddedContentStore.delete(inputIndexedResourceId)
                    .collectList(),
                ioPairStore.get(inputIndexedResourceId)
                    .collectList()
            ).flatMap { (_, ioPairs) ->
                val outputResourceIds = ioPairs.map { it.outputResourceId }
                ioPairStore.delete(inputIndexedResourceId)
                    .then(
                        Flux.fromIterable(outputResourceIds + inputIndexedResourceId)
                            .flatMap(indexedResourceStore::delete)
                            .then()
                    )
            }
        }.then()
    }

    companion object {
        private const val MAX_CONTENT_LENGTH: Int = 65535

        private val logger = LoggerFactory.getLogger(EmbeddedResourcePairStore::class.java)
    }
}
