package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable

data class AlignableDAGNodeAlignmentOperation<T : MetricAlignable<T, TAO>, TAO, L : MetricAlignable<L, LOp>, LOp: Any>(
    val nodeOperation: TAO?,
    val parentEdgeListOperation: NormativeEdgeAlignmentOperation<L, LOp>?,
)