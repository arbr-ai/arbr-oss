package com.arbr.platform.object_graph.alignable

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.SwapAlignable
import com.arbr.platform.alignable.alignable.alignment.Alignment

data class PartialRefAlignable(
    val uuid: String?,
): Alignable<PartialRefAlignable, String?> {
    override fun align(e: PartialRefAlignable): Alignment<PartialRefAlignable, String?> {
        if (this.uuid == e.uuid) {
            return Alignment.Equal(this, e)
        }

        val swapAlign = SwapAlignable(uuid).align(SwapAlignable(e.uuid))

        return Alignment.Align(
            swapAlign.operations,
            swapAlign.cost,
            this,
            e,
        )
    }

    override fun applyAlignment(alignmentOperations: List<String?>): PartialRefAlignable {
        val result = SwapAlignable(uuid).applyAlignment(alignmentOperations).element
        return PartialRefAlignable(result)
    }

    override fun empty(): PartialRefAlignable {
        return PartialRefAlignable(null)
    }

}