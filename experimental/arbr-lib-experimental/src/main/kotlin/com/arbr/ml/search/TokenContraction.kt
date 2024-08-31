package com.arbr.platform.ml.search

data class TokenContraction(
    // Combined
    val token: String,
    val leftChild: String,
    val rightChild: String,
)