package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.optimization.model.BindingParameter

interface ParameterValueProvider {

    fun getValue(kind: NamedMetricKind): Double

    fun getParameterMap(): Map<NamedMetricKind, BindingParameter<Double>>

    fun getWeightVector(): ColumnVector<Dim.VariableN>

    fun getParameterWeightVector(costMetricKind: NamedMetricKind): ColumnVector<Dim.VariableN>

    fun getParameterValue(kind: NamedMetricKind): ParameterValue {
        return ParameterValue(getValue(kind))
    }
}
