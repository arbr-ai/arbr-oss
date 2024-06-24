package com.arbr.kafka.differential_models

interface DifferentialModelReducer<S, D> {

    fun reduce(trackedState: DifferentialModelTrackedState<S>, diff: D): DifferentialModelTrackedState<S>

    /**
     * Associatively combine a diff with some number of other diffs.
     */
    fun associate(diff: D, otherDiffs: List<D>): D

    /**
     * Associatively combine a pair of diffs.
     */
    fun associate(diff: D, otherDiff: D): D {
        return associate(diff, listOf(otherDiff))
    }

    /**
     * Given a list of diffs defining construction of state against the given initial state, produce a list of
     * consistent tracked diff applications comprising a state transition replay.
     */
    fun reconstructLedger(
        initialState: DifferentialModelTrackedState<S>,
        diffs: List<D>
    ): List<DifferentialModelTrackedDiffApplication<D>> {
        var trackedState = initialState

        val trackedDiffApplications = mutableListOf<DifferentialModelTrackedDiffApplication<D>>()
        for (diff in diffs) {

            val nextState = reduce(trackedState, diff)
            trackedDiffApplications.add(
                DifferentialModelTrackedDiffApplication(
                    trackedState.hash,
                    diff,
                    nextState.hash,
                )
            )
            trackedState = nextState
        }
        return trackedDiffApplications
    }
}
