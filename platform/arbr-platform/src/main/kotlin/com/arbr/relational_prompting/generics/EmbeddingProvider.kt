package com.arbr.relational_prompting.generics

import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingRequest
import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingResponse
import reactor.core.publisher.Mono

interface EmbeddingProvider {

    fun getEmbedding(
        request: OpenAiEmbeddingRequest
    ): Mono<OpenAiEmbeddingResponse>
}
