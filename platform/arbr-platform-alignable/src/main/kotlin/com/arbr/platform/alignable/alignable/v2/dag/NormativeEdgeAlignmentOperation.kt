package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.ml.optimization.base.ParameterValue
import java.util.*

data class NormativeEdgeAlignmentOperation<L : MetricAlignable<L, LOp>, LOp>(
    val labelAlignment: MetricAlignment<Optional<L>, Optional<LOp>>,
    val netNorm: ParameterValue,
)