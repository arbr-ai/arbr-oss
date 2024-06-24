package com.arbr.relational_prompting.services.embedding.client

import com.arbr.relational_prompting.generics.EmbeddingProvider
import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingRequest
import com.arbr.content_formats.tokens.TokenizationUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class EmbeddingClient(
    private val embeddingProvider: EmbeddingProvider,
) {
    private fun isValidInput(content: String): Boolean {
        // TODO: Add more checks
        return content.isNotBlank()
    }

    fun preprocess(content: String): String {
        return if (TokenizationUtils.getTokenCount(content) > embeddingTokenLimit) {
            val prefixIndex = content.indices.toList().binarySearch { i ->
                val prefix = content.take(i + 1)
                TokenizationUtils.getTokenCount(prefix) - embeddingTokenLimit
            }.let {
                if (it >= 0) it else it.inv()
            }
            logger.warn("Truncating embedding argument from length ${content.length} to length $prefixIndex")

            content.take(prefixIndex)
        } else {
            content
        }
    }

    private fun embedInner(contents: List<String>): Mono<List<List<Double>?>> {
        // Map invalid inputs to null while preserving order and index overall
        val indexedContents = contents.withIndex()
            .filter { isValidInput(it.value) }
            .map { (i, v) -> i to preprocess(v) }
        val validContents = indexedContents.map { it.second }
        if (validContents.isEmpty()) {
            return Mono.just(emptyList())
        }

        return embeddingProvider.getEmbedding(
            OpenAiEmbeddingRequest(
                embeddingModel,
                validContents,
                null,
            )
        ).map { resp ->
            val resultList = resp.data
            val placeholderList: Array<List<Double>?> = arrayOfNulls(contents.size)

            for (embeddingResult in resultList) {
                val contentsIdx = indexedContents[embeddingResult.index].first
                placeholderList[contentsIdx] = embeddingResult.embedding
            }

            placeholderList.toList()
        }
    }

    /**
     * Embed a potentially large content sequence, computing in sequential prefetched batches.
     */
    fun embedMany(contents: List<String>): Flux<Pair<Int, List<Double>?>> {
        val windows = contents
            .withIndex()
            .windowed(maxBatchSize, step = maxBatchSize, partialWindows = true)
        return Flux.fromIterable(windows)
            .flatMap({ window ->
                embedInner(window.map { it.value })
                    .map { results ->
                        window.zip(results).map { (iv , result) ->
                            iv.index to result
                        }
                    }
            }, embedManyParallelism)
            .flatMapIterable { results ->
                results
            }
    }

    /**
     * Embed the given strings in a single request.
     */
    fun embed(contents: List<String>): Mono<List<List<Double>?>> {
        return if (contents.isEmpty()) {
            Mono.just(emptyList())
        } else if (contents.size <= maxBatchSize) {
            embedInner(contents)
        } else {
            embedMany(contents)
                .collectList()
                .map { indexedEmbeddings ->
                    indexedEmbeddings.map { (_, vectorOrNull) ->
                        vectorOrNull
                    }
                }
        }
    }

    companion object {
        private const val embeddingModel = "text-embedding-ada-002"
        private const val maxBatchSize = 100
        private const val embeddingTokenLimit = 8191
        private const val embedManyParallelism = 8

        private val logger = LoggerFactory.getLogger(EmbeddingClient::class.java)
    }
}