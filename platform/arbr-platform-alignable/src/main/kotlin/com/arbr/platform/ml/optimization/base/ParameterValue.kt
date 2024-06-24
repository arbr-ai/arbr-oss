package com.arbr.platform.ml.optimization.base

/**
 * Thin wrapper for the value of a parameter.
 */
@JvmInline
value class ParameterValue(val value: Double) {

    operator fun plus(doubleValue: Double): ParameterValue {
        return ParameterValue(value + doubleValue)
    }
    operator fun plus(otherValue: ParameterValue): ParameterValue {
        return ParameterValue(value + otherValue.value)
    }

    operator fun times(doubleValue: Double): ParameterValue {
        return ParameterValue(value * doubleValue)
    }
    operator fun times(otherValue: ParameterValue): ParameterValue {
        return ParameterValue(value * otherValue.value)
    }
}
