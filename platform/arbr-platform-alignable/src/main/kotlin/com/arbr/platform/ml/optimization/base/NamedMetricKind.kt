package com.arbr.platform.ml.optimization.base

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class NamedMetricKind(
    @JsonValue
    val name: String
) {

    override fun toString(): String {
        return "NamedMetricKind(name=$name)"
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun create(
            value: Any
        ): NamedMetricKind {
            return NamedMetricKind(value as String)
        }
    }
}
