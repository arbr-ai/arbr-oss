package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.BindingParameter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

typealias ParameterList = List<BindingParameter<Double>>

fun interface AsyncEvaluator {

    /**
     * Evaluate each parameter list and emit a pair (index, score) where index is the index
     * in the argument list. Implementor may use any appropriate amount of parallelism.
     */
    fun evaluateMany(
        parameterLists: List<ParameterList>,
    ): Flux<Pair<Int, Double>>

    fun evaluate(
        parameters: ParameterList,
    ): Mono<Double> {
        return evaluateMany(listOf(parameters))
            .next()
            .map { it.second }
    }

}

interface AsyncOptimizer {

    fun optimizeAsync(
        parameters: ParameterList,
        evaluator: AsyncEvaluator,
        testDataEvaluator: AsyncEvaluator,
        learningRate: Double = 0.01,
        tolerance: Double = 1e-6,
        maxIterations: Long = 1000L,
        parameterSetListener: ParameterSetListener,
    ): Mono<List<BindingParameter<Double>>>

}
