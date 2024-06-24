package com.arbr.platform.alignable.alignable.collections

import com.fasterxml.jackson.annotation.JsonIgnore

sealed class SequenceAlignmentOperation<E, O>(
    open val atIndex: Int,
    open val alignment: List<O>,
    @JsonIgnore
    open val element: E,
    open val cost: Double,
) {
    val operationName: String = this::class.java.simpleName

    class Insert<E, O>(
        override val atIndex: Int,
        override val alignment: List<O>,
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ): SequenceAlignmentOperation<E, O>(atIndex, alignment, element, cost)

    class Delete<E, O>(
        override val atIndex: Int,
        override val alignment: List<O>,
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ): SequenceAlignmentOperation<E, O>(atIndex, alignment, element, cost)

    class Edit<E, O>(
        override val atIndex: Int,
        override val alignment: List<O>,
        @JsonIgnore
        val fromElement: E, // maybe unnecessary but nice to have
        @JsonIgnore
        override val element: E,
        override val cost: Double,
    ): SequenceAlignmentOperation<E, O>(atIndex, alignment, element, cost)
}
