package com.arbr.ml.search

data class TokenizationLayer(
    val aggregate: TokenContraction,
    val tokenizedDocument: List<TaggedToken>,
    val singleTokenCounts: Map<String, Int>,
    val singleTokenObservations: Map<String, Pair<Int, Int>>,
    val tokenPairCounts: Map<Pair<String, String>, Int>,
    val tokenPairClasses: Map<Pair<String, String>, Pair<Int, Int>>,
)
