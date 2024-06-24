package com.arbr.ml.search

data class TaggedToken(
    val token: String,
    val positivity: Int,
    val negativity: Int,
)

data class TaggedCodeToken(
    val code: Int,
    val positivity: Int,
    val negativity: Int,
)
