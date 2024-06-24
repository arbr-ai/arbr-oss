package com.arbr.platform.alignable.alignable

import com.arbr.platform.alignable.alignable.alignment.Alignment

data class SwapAlignable<T>(val element: T) : Alignable<SwapAlignable<T>, T> {
    override fun align(e: SwapAlignable<T>): Alignment<SwapAlignable<T>, T> {
        return if (element == e.element) {
            // Equal
            Alignment.Equal(this, e)
        } else {
            // Replace
            return Alignment.Align(
                listOf(e.element),
                1.0,
                this,
                e,
            )
        }
    }

    override fun empty(): SwapAlignable<T> {
        return this
    }

    override fun applyAlignment(
        alignmentOperations: List<T>
    ): SwapAlignable<T> {
        return if (alignmentOperations.isEmpty()) {
            this
        } else {
            SwapAlignable(alignmentOperations.last())
        }
    }
}