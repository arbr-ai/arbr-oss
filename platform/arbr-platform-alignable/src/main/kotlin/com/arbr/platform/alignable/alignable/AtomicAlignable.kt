package com.arbr.platform.alignable.alignable

import com.arbr.platform.alignable.alignable.alignment.Alignment

@Suppress("DataClassPrivateConstructor")
data class AtomicAlignable<T> private constructor(
    val element: T,
    private val isEmpty: Boolean,
) : Alignable<AtomicAlignable<T>, T> {

    constructor(element: T) : this(element, isEmpty = false)

    override fun align(e: AtomicAlignable<T>): Alignment<AtomicAlignable<T>, T> {
        return if (element == e.element) {

            if (isEmpty == e.isEmpty) {
                // Equal
                Alignment.Equal(this, e)
            } else {
                // Align
                Alignment.Align(
                    listOf(element),
                    1.0,
                    this,
                    e,
                )
            }
        } else {
            // Unequal
            throw NoViableAlignmentException(
                "Atomic elements not equal",
                element,
                e.element,
            )
        }
    }

    override fun empty(): AtomicAlignable<T> {
        return this.copy(isEmpty = true)
    }

    override fun applyAlignment(
        alignmentOperations: List<T>
    ): AtomicAlignable<T> {
        return this
    }
}