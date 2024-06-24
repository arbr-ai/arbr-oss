package com.arbr.util.invariants

data class InvariantsConfig(
    val enabled: Boolean,
    val failureLevel: Invariants.FailureLevel,
)