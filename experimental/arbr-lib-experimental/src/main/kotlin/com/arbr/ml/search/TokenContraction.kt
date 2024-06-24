package com.arbr.ml.search

data class TokenContraction(
    // Combined
    val token: String,
    val leftChild: String,
    val rightChild: String,
)