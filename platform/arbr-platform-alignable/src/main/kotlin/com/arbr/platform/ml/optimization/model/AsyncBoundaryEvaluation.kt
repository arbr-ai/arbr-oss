package com.arbr.platform.ml.optimization.model

data class AsyncBoundaryEvaluation(
    val passed: Boolean,
    val score: Double,
    val computedDecisionThresholds: List<Double>,
)