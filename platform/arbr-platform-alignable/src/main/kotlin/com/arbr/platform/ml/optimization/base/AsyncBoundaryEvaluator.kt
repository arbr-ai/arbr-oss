package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.AsyncBoundaryEvaluation
import com.arbr.platform.ml.optimization.model.BindingParameter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun interface AsyncBoundaryEvaluator {

    fun evaluateMany(
        parameterMaps: List<Map<NamedMetricKind, BindingParameter<Double>>>,
        targetParameterKind: NamedMetricKind?,
    ): Flux<Pair<Int, AsyncBoundaryEvaluation>>

    fun evaluate(
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        targetParameterKind: NamedMetricKind?,
    ): Mono<AsyncBoundaryEvaluation> {
        return evaluateMany(listOf(parameters), targetParameterKind)
            .next()
            .map { it.second }
    }

}