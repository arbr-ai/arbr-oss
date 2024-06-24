package com.arbr.relational_prompting.generics

import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingRequest
import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingResponse
import reactor.core.publisher.Mono

class DefaultEmbeddingProvider: EmbeddingProvider {

    override fun getEmbedding(
        request: OpenAiEmbeddingRequest
    ): Mono<OpenAiEmbeddingResponse> {
        return Mono.error(NotImplementedError("No embedding provider configured"))
    }
}
