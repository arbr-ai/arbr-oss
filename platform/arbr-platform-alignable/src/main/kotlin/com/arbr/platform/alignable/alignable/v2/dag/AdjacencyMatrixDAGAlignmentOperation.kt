package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.ml.optimization.base.ParameterValue

/**
 * Alignment operation on a DAG of operations of type O, each of which is alignable by type O2
 */
data class AdjacencyMatrixDAGAlignmentOperation<O : MetricAlignable<O, O2>, O2>(
    val kind: AdjacencyMatrixDAGAlignmentKind,
    val cost: ParameterValue,
    val targetElement: O?,
    val sourceElement: O?,
) {
    override fun toString(): String {
//        return "${kind.name.padStart(9)}[${String.format("%.08f", cost)}]: $sourceElement -> $targetElement"
        return "${kind.name.padStart(9)}[$cost]: $sourceElement -> $targetElement"
    }
}