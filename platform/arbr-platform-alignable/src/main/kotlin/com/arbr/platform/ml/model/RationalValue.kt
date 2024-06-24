package com.arbr.platform.ml.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class RationalValue(
    val numerator: Long,
    val denominator: Long,
) {

    @JsonIgnore
    val doubleValue: Double = numerator * 1.0 / denominator

    companion object {
        private const val DEFAULT_PRECISION = 1_000_000L

        fun ofDouble(doubleValue: Double): RationalValue {
            val numerator = (doubleValue * DEFAULT_PRECISION).toLong()
            return RationalValue(numerator, DEFAULT_PRECISION)
        }
    }
}