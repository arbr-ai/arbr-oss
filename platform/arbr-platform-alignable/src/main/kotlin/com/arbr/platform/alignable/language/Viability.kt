package com.arbr.platform.alignable.language

import com.arbr.platform.alignable.alignable.edit_operation.EditOperation


interface Viability<Q, E: EditOperation<Q>> {

    val initialState: Q

    fun isViableEdit(fromState: Q, edit: E): Boolean

    /**
     * States, if any, which are viable for an induce operation given the current state and a proposed (non-viable) edit
     */
    fun inducibleEdits(fromState: Q, proposedEdit: E): List<E>

    /**
     * States, if any, which are viable for a deduce operation given the current state
     */
    fun deducibleEdits(fromState: Q, proposedEdit: E): List<E>

    /**
     * Provide a code uniquely identifying the state in the edit graph for memoization.
     */
    fun stateCode(state: Q): Int

    companion object {
        // TODO: Deprecate
        fun <Q, E: EditOperation<Q>> of(q0: Q, f: (Q) -> List<Q>) = object : Viability<Q, E> {
            override val initialState: Q = q0

            override fun isViableEdit(fromState: Q, edit: E): Boolean {
                return edit.applyTo(fromState) in f(fromState)
            }

            override fun inducibleEdits(fromState: Q, proposedEdit: E): List<E> {
                return emptyList()
            }

            override fun deducibleEdits(fromState: Q, proposedEdit: E): List<E> {
                return emptyList()
            }

            override fun stateCode(state: Q): Int {
                return state.hashCode()
            }
        }
    }
}
