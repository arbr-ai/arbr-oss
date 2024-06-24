package com.arbr.engine.services.embedding.client

data class SimpleEmbeddingSearchResult(
    val documentIndex: Int,
    val embeddingDistance: Double,
)