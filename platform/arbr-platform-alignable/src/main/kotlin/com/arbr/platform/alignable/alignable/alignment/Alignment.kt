package com.arbr.platform.alignable.alignable.alignment

import com.arbr.platform.data_structures_common.partial_order.LinearOrderList

sealed class Alignment<E, AlignmentOperation>(
    open val operations: List<AlignmentOperation>,

    // Cost of edit
    override val cost: Double,

    // The source element which was the input to the operation.
    override val sourceElement: E,

    // The target element which resulted from the operation.
    override val targetElement: E,
): OrderAlignment<E, AlignmentOperation, LinearOrderList<AlignmentOperation>> {

    override val operationOrder: LinearOrderList<AlignmentOperation>?
        get() = LinearOrderList(operations)

    data class Align<E, AlignmentOperation>(
        override val operations: List<AlignmentOperation>,

        // Cost of edit
        override val cost: Double,

        // The source element which was the input to the operation.
        override val sourceElement: E,

        // The target element which resulted from the operation.
        override val targetElement: E,
    ): Alignment<E, AlignmentOperation>(operations, cost, sourceElement, targetElement)

    data class Equal<E, AlignmentOperation>(
        // The source element which was the input to the operation.
        override val sourceElement: E,

        // The target element which resulted from the operation.
        override val targetElement: E,
    ): Alignment<E, AlignmentOperation>(emptyList(), 0.0, sourceElement, targetElement)

    companion object {

        fun <E, AlignmentOperation> of(
            operations: List<AlignmentOperation>,
            cost: Double,
            sourceElement: E,
            targetElement: E,
        ): Alignment<E, AlignmentOperation> {
            return if (operations.isEmpty()) {
                Equal(sourceElement, targetElement)
            } else {
                Align(operations, cost, sourceElement, targetElement)
            }
        }

    }
}
