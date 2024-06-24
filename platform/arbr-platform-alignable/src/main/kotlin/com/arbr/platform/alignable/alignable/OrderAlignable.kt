package com.arbr.platform.alignable.alignable

import com.arbr.platform.alignable.alignable.alignment.OrderAlignment
import com.arbr.platform.data_structures_common.partial_order.PartialOrder


interface OrderAlignable<E : OrderAlignable<E, AlignmentOperation, Order, AlignmentType>, AlignmentOperation, Order: PartialOrder<AlignmentOperation>, AlignmentType: OrderAlignment<E, AlignmentOperation, Order>> {
    /**
     * Align against `e` if possible, or else throw NoViableAlignmentException.
     */
    fun align(e: E): AlignmentType

    /**
     * Align against `e` if possible, or else return null.
     */
    fun alignOrNull(e: E): AlignmentType? {
        return try {
            align(e)
        } catch (e: NoViableAlignmentException) {
            null
        }
    }

    /**
     * Give an empty value such that alignment of empty to this value represents construction of the value.
     */
    fun empty(): E

    fun applyAlignment(
        alignmentOperations: Order
    ): E

    /**
     * Norm of the alignable object, defined as |x - e| where x is this object, e is empty, || is cost of an edit
     * Equivalent to "construction cost"
     */
    fun norm(): Double {
        @Suppress("UNCHECKED_CAST")
        return empty().align(this as E).cost
    }
}