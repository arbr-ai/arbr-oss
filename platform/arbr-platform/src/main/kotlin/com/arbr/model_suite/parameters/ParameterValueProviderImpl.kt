package com.arbr.model_suite.parameters

import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentParameterSpec
import com.arbr.util_common.collections.mapToArray
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.impl.TypedColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.base.ParameterValueProvider
import com.arbr.ml.optimization.model.BindingParameter

class ParameterValueProviderImpl private constructor(
    private val parameterMap: MutableMap<String, Double>, // All n^2 params
    private val defaultValue: Double?,
    @Suppress("UNUSED_PARAMETER") jvmDiscriminator: Unit,
) : ParameterValueProvider {

    private val weightVector = TypedColumnVector<Dim.VariableN>(
        DocumentDiffAlignmentParameterSpec.parameterKinds.mapToArray { namedMetricKind ->
            getValue(namedMetricKind)
        }.toDoubleArray(),
        Dim.VariableN,
    )

    private val parameterWeightVectors = DocumentDiffAlignmentParameterSpec.parameterKinds.withIndex().associate { (i, metric) ->
        metric.name to TypedColumnVector<Dim.VariableN>(
            DoubleArray(DocumentDiffAlignmentParameterSpec.parameterKinds.size) { j ->
                if (j == i) {
                    1.0
                } else {
                    0.0
                }
            },
            Dim.VariableN,
        )
    }

    constructor(
        parameterMetricMap: Map<NamedMetricKind, BindingParameter<Double>>,  // All n^2 params
        defaultValue: Double? = null,
    ) : this(
        parameterMetricMap
            .mapKeys { it.key.name }
            .mapValues { it.value.value }
            .toMutableMap(),
        defaultValue,
        Unit,
    )

    init {
        if (defaultValue != null) {
            DocumentDiffAlignmentParameterSpec.parameterKinds.forEach { kind ->
                if (kind.name !in parameterMap) {
                    parameterMap[kind.name] = defaultValue
                }
            }
        }
    }

    override fun getValue(kind: NamedMetricKind): Double {
        return parameterMap[kind.name] ?: (
                defaultValue
                    ?: throw Exception("Missing required model parameter: ${kind.name}")
                )
    }

    override fun getParameterMap(): Map<NamedMetricKind, BindingParameter<Double>> {
        return parameterMap
            .mapKeys { NamedMetricKind(it.key) }
            .mapValues { BindingParameter(it.key, it.value) }
    }

    override fun getWeightVector(): ColumnVector<Dim.VariableN> {
        return weightVector
    }

    override fun getParameterWeightVector(costMetricKind: NamedMetricKind): ColumnVector<Dim.VariableN> {
        // If this throws, the given metric is missing from the global list
        return parameterWeightVectors[costMetricKind.name]!!
    }

}