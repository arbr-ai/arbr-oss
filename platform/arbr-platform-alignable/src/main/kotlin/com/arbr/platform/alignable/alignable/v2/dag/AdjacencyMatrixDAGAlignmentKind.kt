package com.arbr.platform.alignable.alignable.v2.dag

import com.fasterxml.jackson.annotation.JsonIgnore
import com.arbr.platform.alignable.alignable.ADJ_DEDUCE
import com.arbr.platform.alignable.alignable.ADJ_INDUCE
import com.arbr.platform.alignable.alignable.ADJ_MERGE
import com.arbr.platform.ml.optimization.base.NamedMetricKind

enum class AdjacencyMatrixDAGAlignmentKind {
    MERGE,
    INDUCE,
    DEDUCE;

    @JsonIgnore
    fun costMetricKind(): NamedMetricKind {
        return when (this) {
            MERGE -> ADJ_MERGE
            INDUCE -> ADJ_INDUCE
            DEDUCE -> ADJ_DEDUCE
        }
    }
}
