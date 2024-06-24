package com.arbr.engine.services.embedding.dal

import com.arbr.engine.services.vector_db.model.Namespace
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.services.ai_application.config.ResourceEmbeddingContent
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EmbeddedResourcePairDAL {
    fun retrieveNearestNeighbors(
        embeddingContents: List<ResourceEmbeddingContent>,
        namespace: Namespace,
        numNeighbors: Long,
    ): Flux<EmbeddedResourcePair>

    fun getEmbeddedResourcePairByVectorId(
        vectorId: String,
    ): Mono<EmbeddedResourcePair>

    fun updateEmbeddedResourcePair(
        vectorId: String,
        namespace: Namespace,
        newChatMessages: List<ChatMessage>,
        newTier: Long,
    ): Mono<Void>

    fun deleteEmbeddedResourcePair(
        vectorId: String,
        namespace: Namespace,
    ): Mono<Void>
}
