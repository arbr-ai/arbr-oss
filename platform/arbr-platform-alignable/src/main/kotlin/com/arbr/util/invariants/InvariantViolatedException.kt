package com.arbr.util.invariants

class InvariantViolatedException(override val message: String?, override val cause: Throwable?): Exception(message, cause)