package com.arbr.platform.alignable.alignable.v2.dag

/**
 * Immutable DAG + Parallel list of typed node values
 */
data class AdjacencyMatrixDAGValued<V>(
    val matrix: List<List<Boolean>>,
    val values: List<V>,
)
