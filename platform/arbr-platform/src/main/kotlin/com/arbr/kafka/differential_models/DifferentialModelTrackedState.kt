package com.arbr.kafka.differential_models

data class DifferentialModelTrackedState<S>(
    val model: S,
    val hash: String,
)

