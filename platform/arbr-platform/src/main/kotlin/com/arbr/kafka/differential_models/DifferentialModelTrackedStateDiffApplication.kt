package com.arbr.kafka.differential_models

data class DifferentialModelTrackedStateDiffApplication<S, D>(
    val priorTrackedState: DifferentialModelTrackedState<S>,
    val diff: D,
    val nextTrackedState: DifferentialModelTrackedState<S>,
)