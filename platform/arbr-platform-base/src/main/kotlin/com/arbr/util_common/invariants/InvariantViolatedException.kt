package com.arbr.util_common.invariants

class InvariantViolatedException(override val message: String?, override val cause: Throwable?): Exception(message, cause)