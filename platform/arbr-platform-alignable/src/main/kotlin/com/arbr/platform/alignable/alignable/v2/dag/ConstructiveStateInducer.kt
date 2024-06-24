package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import java.util.*

interface ConstructiveStateInducer<Q : Any, StateOp : MetricAlignable<StateOp, StateOpAlignmentOp>, StateOpAlignmentOp> {
    val initialState: Q
    val initialStateCode: Int

    /**
     * Operations, if any, which are viable for transitions given the current state and a pair of (potentially
     * non-viable) edits.
     */
    fun viableOperations(
        fromState: Q,
        targetOperation: Optional<StateOp>,
        targetIsNonEmpty: Boolean,
        targetIsComplete: Boolean,
        sourceOperation: Optional<StateOp>,
        sourceIsNonEmpty: Boolean,
        sourceIsComplete: Boolean,
    ): List<MetricAlignment<StateOp, StateOpAlignmentOp>>

    /**
     * Apply the operation the given state and state code, returning the next state and updated code.
     * State constructions need not be recoverable from the code, but it should act like a hash code governing distinct
     * states.
     */
    fun apply(state: Q, stateCode: Int, operation: StateOp): Pair<Q, Int>
}