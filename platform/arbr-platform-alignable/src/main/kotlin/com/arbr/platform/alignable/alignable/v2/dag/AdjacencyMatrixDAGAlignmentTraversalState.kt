package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable

data class AdjacencyMatrixDAGAlignmentTraversalState<Q : Any, O : MetricAlignable<O, O2>, O2>(
    val targetState: Set<Int>,
    val targetStateCode: Int,
    val targetIsComplete: Boolean,
    val sourceState: Set<Int>,
    val sourceStateCode: Int,
    val sourceIsComplete: Boolean,
    val constructionStateCode: Int,

    /**
     * Metadata not associated with equality or hashing
     */
    val constructionState: Q,
    val latestOperation: AdjacencyMatrixDAGAlignmentOperation<O, O2>? = null,
) {

    private val hashCodeValue by lazy {
        // Including construction code explodes runtime
        // We do lose some value by omitting it, since we're requiring that the shortest path to a node results in the
        // best downstream outcome, which is not always true given that the construction dictates what states are valid
        // next
        // One option is to limit the state multiplicity here based on what options are viable next rather than the
        // entire construction.
//        constructionStateCode * 179 + targetStateCode * 37 + sourceStateCode * 61
        targetStateCode * 37 + sourceStateCode * 61
    }

    override fun hashCode(): Int {
        return hashCodeValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AdjacencyMatrixDAGAlignmentTraversalState<*, *, *>

        return hashCodeValue == other.hashCodeValue
    }
}