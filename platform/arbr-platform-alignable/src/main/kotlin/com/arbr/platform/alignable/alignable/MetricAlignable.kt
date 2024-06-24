package com.arbr.platform.alignable.alignable

import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.data_structures_common.partial_order.LinearOrderList

interface MetricAlignable<E : MetricAlignable<E, AlignmentOperation>, AlignmentOperation>:
    OrderAlignable<E, AlignmentOperation, LinearOrderList<AlignmentOperation>, MetricAlignment<E, AlignmentOperation>> {

    fun applyAlignment(
        alignmentOperations: List<AlignmentOperation>
    ): E

    override fun applyAlignment(alignmentOperations: LinearOrderList<AlignmentOperation>): E {
        return applyAlignment(alignmentOperations.toList())
    }
}