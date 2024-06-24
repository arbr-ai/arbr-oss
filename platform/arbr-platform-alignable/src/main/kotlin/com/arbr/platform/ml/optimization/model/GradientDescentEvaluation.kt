package com.arbr.platform.ml.optimization.model

data class GradientDescentEvaluation(
    /**
     * Loss, complement of score, 0 is perfect, 1 is completely wrong
     */
    val loss: Double,
)