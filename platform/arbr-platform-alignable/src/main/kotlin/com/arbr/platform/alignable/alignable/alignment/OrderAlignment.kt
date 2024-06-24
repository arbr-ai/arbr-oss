package com.arbr.platform.alignable.alignable.alignment

import com.arbr.platform.data_structures_common.partial_order.PartialOrder

/**
 * An alignment in which the operations have an implied order. In all cases at the time of writing, this order is a
 * list, but the thought of operation trees with independent branches is interesting.
 *
 * Importantly, this is not always related to the underlying data structure being aligned - a graph can imply a linear
 * order, and a list can be compatible with an operation tree, based on the dependency semantics of the operations.
 */
sealed interface OrderAlignment<E, AlignmentOperation, Order: PartialOrder<AlignmentOperation>> {

    val operationOrder: Order?

    // Cost of edit
    val cost: Double

    // The source element which was the input to the operation.
    val sourceElement: E?

    // The target element which resulted from the operation.
    val targetElement: E
}
