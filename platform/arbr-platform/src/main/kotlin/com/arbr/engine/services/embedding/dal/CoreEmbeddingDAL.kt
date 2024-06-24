package com.arbr.engine.services.embedding.dal

import com.arbr.engine.services.db.model.VectorQueryResult
import com.arbr.engine.services.embedding.model.InsertedCoreEmbedding
import com.arbr.engine.services.vector_db.model.Namespace
import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import reactor.core.publisher.Flux

abstract class CoreEmbeddingDAL {

    /**
     * Publish a list of resources and return the created resources.
     */
    abstract fun publishAll(
        inputs: List<ResourceEmbeddingContent>,
        versionId: String,
        namespace: Namespace,
    ): Flux<InsertedCoreEmbedding>

    /**
     * Publish and example and return the created resources.
     */
    fun publish(
        input: ResourceEmbeddingContent,
        versionId: String,
        namespace: Namespace,
    ): Flux<InsertedCoreEmbedding> = publishAll(listOf(input), versionId, namespace)

    abstract fun retrieveNearestNeighbors(
        input: ResourceEmbeddingContent,
        versionId: String,
        namespace: Namespace,
        numNeighborsToRetrieve: Long,
    ): Flux<VectorQueryResult>
}
