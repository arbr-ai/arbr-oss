package com.arbr.platform.ml.optimization.model

data class ParameterAdmissibleRanges(
    val parameter: BindingParameter<Double>,
    val score: Double?,
    val ranges: List<ParameterIntervalResult>
)