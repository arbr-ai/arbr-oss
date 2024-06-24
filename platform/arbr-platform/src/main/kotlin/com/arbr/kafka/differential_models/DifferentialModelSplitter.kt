package com.arbr.kafka.differential_models

fun interface DifferentialModelSplitter<S, D> {

    /**
     * Associatively combine a diff with some number of other diffs.
     */
    fun split(state: S): List<D>
}
