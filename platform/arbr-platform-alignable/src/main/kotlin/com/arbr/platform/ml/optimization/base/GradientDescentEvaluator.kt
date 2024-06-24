package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.GradientDescentEvaluation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun interface GradientDescentEvaluator {

    fun evaluateMany(
        parameterMaps: List<Map<NamedMetricKind, BindingParameter<Double>>>,
    ): Flux<Pair<Int, GradientDescentEvaluation>>

    fun evaluate(
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
    ): Mono<GradientDescentEvaluation> {
        return evaluateMany(listOf(parameters))
            .next()
            .map { it.second }
    }

}