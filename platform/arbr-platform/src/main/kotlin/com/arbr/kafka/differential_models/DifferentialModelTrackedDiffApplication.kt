package com.arbr.kafka.differential_models

data class DifferentialModelTrackedDiffApplication<D>(
    val priorHash: String,
    val diff: D,
    val nextHash: String,
)