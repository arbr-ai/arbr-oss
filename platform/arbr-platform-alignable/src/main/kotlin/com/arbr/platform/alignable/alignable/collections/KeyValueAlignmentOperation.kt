package com.arbr.platform.alignable.alignable.collections

import com.fasterxml.jackson.annotation.JsonIgnore

sealed class KeyValueAlignmentOperation<K, E, O>(
    open val atKey: K,
    open val alignment: List<O>,
    @JsonIgnore
    open val element: E,
    open val cost: Double,
) {
    val operationName: String = this::class.java.simpleName

    class Insert<K, E, O>(
        override val atKey: K,
        override val alignment: List<O>,
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ) : KeyValueAlignmentOperation<K, E, O>(atKey, alignment, element, cost) {
        override fun toString(): String {
            return "Insert[$atKey] ${element.toString().take(32)}..."
        }
    }

    class Delete<K, E, O>(
        override val atKey: K,
        override val alignment: List<O>,
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ) : KeyValueAlignmentOperation<K, E, O>(atKey, alignment, element, cost) {
        override fun toString(): String {
            return "Delete[$atKey] ${element.toString().take(32)}..."
        }
    }

    class Edit<K, E, O>(
        override val atKey: K,
        override val alignment: List<O>,
        @JsonIgnore
        val fromElement: E,
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ) : KeyValueAlignmentOperation<K, E, O>(atKey, alignment, element, cost) {
        override fun toString(): String {
            return "Edit[$atKey] {\n${
                alignment.joinToString("\n") { it.toString() }.split("\n").joinToString("\n") { "\t" + it }
            }\n}"
        }
    }
}