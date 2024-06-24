package com.arbr.ml.search

data class TokenCodeContraction(
    // Combined via pairing function
    val code: Int,
    val leftCode: Int,
    val rightCode: Int,
) {
    companion object {
        private const val PRIME = 179

        fun pair(leftCode: Int, rightCode: Int): TokenCodeContraction {
            val code = (leftCode + 1) * PRIME + rightCode + 7
            return TokenCodeContraction(code, leftCode, rightCode)
        }
    }
}