package com.arbr.platform.ml.optimization.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.arbr.platform.ml.optimization.base.NamedMetricKind
import com.arbr.platform.ml.optimization.model.BindingParameter

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ScoredParameterSet(
    val name: String,
    val parameters: Map<NamedMetricKind, BindingParameter<Double>>,
    val trainingScore: Double,
    val testScore: Double,
)
