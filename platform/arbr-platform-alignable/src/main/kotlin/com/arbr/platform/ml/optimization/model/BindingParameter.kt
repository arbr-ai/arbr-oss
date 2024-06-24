package com.arbr.platform.ml.optimization.model

import com.arbr.platform.ml.optimization.base.ImmutableParameter
import com.arbr.platform.ml.optimization.base.NamedMetricKind

data class BindingParameter<T>(
    val metricKind: NamedMetricKind,
    override val value: T
): ImmutableParameter<T>