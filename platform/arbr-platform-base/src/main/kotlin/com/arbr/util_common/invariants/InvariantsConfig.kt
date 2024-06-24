package com.arbr.util_common.invariants

data class InvariantsConfig(
    val enabled: Boolean,
    val failureLevel: Invariants.FailureLevel,
)