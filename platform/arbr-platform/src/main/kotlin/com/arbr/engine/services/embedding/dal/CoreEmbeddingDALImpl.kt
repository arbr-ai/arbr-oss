package com.arbr.engine.services.embedding.dal

import com.arbr.engine.services.db.client.VectorEmbeddingStore
import com.arbr.engine.services.db.model.VectorQueryResult
import com.arbr.engine.services.embedding.model.InsertedCoreEmbedding
import com.arbr.engine.services.hasher.DocumentHasher
import com.arbr.engine.services.vector_db.model.Namespace
import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import com.arbr.relational_prompting.services.embedding.client.EmbeddingClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CoreEmbeddingDALImpl(
    private val embeddingClient: EmbeddingClient,
    private val vectorEmbeddingStore: VectorEmbeddingStore,
    private val documentHasher: DocumentHasher,
) : CoreEmbeddingDAL() {
    private fun vectorId(
        contentHash: String,
        schemaHash: String,
    ) = "vec_${schemaHash}_$contentHash"

    /**
     * Publish new entries and return all items, not necessarily in order.
     * TODO: Factor out common functionality with the PairDALImpl
     */
    private fun getOrPublish(
        embeddingContents: List<ResourceEmbeddingContent>,
        versionId: String,
        namespace: Namespace,
    ): Flux<InsertedCoreEmbedding> {
        val vectorIdsToResourceEmbeddingContent = embeddingContents.associateBy { embeddingContent ->
            val schemaId = embeddingContent.schemaId
            val schemaHash = documentHasher.hashContents(schemaId.toByteArray()).takeLast(6)
            val hash = documentHasher.hashContents(embeddingContent.content.toByteArray())
            vectorId(hash, schemaHash)
        }

        val batchSize = 1000
        return vectorEmbeddingStore.getMany(vectorIdsToResourceEmbeddingContent.keys.toList(), versionId, namespace,
            batchSize
        )
            .collectList()
            .flatMapMany { existingVectorEmbeddings ->
                val existingEmbeddingContents = existingVectorEmbeddings.mapNotNull { vectorEmbedding ->
                    vectorIdsToResourceEmbeddingContent[vectorEmbedding.vectorId]?.let { resourceEmbeddingContent ->
                        InsertedCoreEmbedding(
                            resourceEmbeddingContent,
                            vectorEmbedding,
                        )
                    }
                }
                val existingVectorIds = existingEmbeddingContents.map { it.coreEmbedding.vectorId }
                val remainingContentToEmbed = embeddingContents.zip(vectorIdsToResourceEmbeddingContent.keys)
                    .filter { it.second !in existingVectorIds }

                val preprocessedContents = remainingContentToEmbed.map { embeddingClient.preprocess(it.first.content) }
                embeddingClient.embedMany(preprocessedContents)
                    .flatMap({ (index, vector) ->
                        if (vector != null) {
                            val embeddingContentPair = remainingContentToEmbed[index]
                            val (embeddingContent, vectorId) = embeddingContentPair
                            val preprocessedEmbeddingContent = preprocessedContents[index]

                            vectorEmbeddingStore.insert(
                                vectorId,
                                versionId,
                                namespace,
                                embeddingContent.schemaId,
                                preprocessedEmbeddingContent,
                                vector.toTypedArray(),
                            ).map { vectorEmbedding ->
                                InsertedCoreEmbedding(embeddingContent, vectorEmbedding)
                            }
                        } else {
                            Mono.empty()
                        }
                    }, VECTOR_INSERT_PARALLELISM)
                    .concatWith(Flux.fromIterable(existingEmbeddingContents))
            }
    }

    override fun publishAll(
        inputs: List<ResourceEmbeddingContent>,
        versionId: String,
        namespace: Namespace
    ): Flux<InsertedCoreEmbedding> {
        return getOrPublish(inputs, versionId, namespace)
    }

    override fun retrieveNearestNeighbors(
        input: ResourceEmbeddingContent,
        versionId: String,
        namespace: Namespace,
        numNeighborsToRetrieve: Long,
    ): Flux<VectorQueryResult> {
        if (numNeighborsToRetrieve == 0L) {
            return Flux.empty()
        }

        return getOrPublish(listOf(input), versionId, namespace)
            .flatMap { insertedCoreEmbedding ->
                vectorEmbeddingStore.getNearestNeighbors(
                    insertedCoreEmbedding.coreEmbedding.embedding.data,
                    namespace,
                    input.schemaId,
                    numNeighborsToRetrieve,
                )
            }
    }

    companion object {
        private const val VECTOR_INSERT_PARALLELISM = 32
    }
}