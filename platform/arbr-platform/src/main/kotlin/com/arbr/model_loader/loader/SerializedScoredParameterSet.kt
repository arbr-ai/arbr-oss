package com.arbr.model_loader.loader

import com.arbr.data_common.base.DataRecordObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.arbr.ml.math.model.RationalValue
import com.arbr.ml.optimization.model.BindingParameter

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SerializedScoredParameterSet(
    val sha: String,
    val name: String,
    val parameters: List<BindingParameter<RationalValue>>,
    val trainingScore: RationalValue,
    val testScore: RationalValue,
): DataRecordObject