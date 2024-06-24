package com.arbr.engine.services.embedding.client

import com.arbr.relational_prompting.generics.EmbeddingProvider
import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.math.pow
import kotlin.math.sqrt

@Component
class SimpleEmbeddingSearchClient(
    private val embeddingProvider: EmbeddingProvider,
) {

    /**
     * Perform a simple embedding search among the given documents and give the indexes and distances of the k nearest
     * results.
     *
     * Supports only OpenAI at the moment and defaults to `text-embedding-ada-002`. Nearest neighbor is L2 and done
     * in-memory, so not suitable for huge applications.
     */
    fun searchNearest(
        query: String,
        documents: List<String>,
        k: Int,
        embeddingModel: String = DEFAULT_EMBEDDING_MODEL
    ): Mono<List<SimpleEmbeddingSearchResult>> {
        return embeddingProvider.getEmbedding(
            OpenAiEmbeddingRequest(
                DEFAULT_EMBEDDING_MODEL,
                listOf(query) + documents,
                null,
            )
        ).map { resp ->
            val targetEmbeddingVectors = resp.data.drop(1).map { it.embedding }
            val queryEmbeddingVector = resp.data.first().embedding

            val distances = targetEmbeddingVectors.mapIndexed { index, vector ->
                val distance = queryEmbeddingVector
                    .zip(vector) { x0, x1 -> (x0 - x1).pow(2) }
                    .sum()
                SimpleEmbeddingSearchResult(
                    index,
                    sqrt(distance),
                )
            }

            distances
                .sortedBy { it.embeddingDistance }
                .take(k)
        }
    }

    companion object {
        private const val DEFAULT_EMBEDDING_MODEL = "text-embedding-ada-002"
    }
}