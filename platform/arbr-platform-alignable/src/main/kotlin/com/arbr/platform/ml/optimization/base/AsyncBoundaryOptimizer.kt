package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.ParameterAdmissibleRanges
import reactor.core.publisher.Mono

interface AsyncBoundaryOptimizer {

    fun optimizeBoundariesAsync(
        sessionName: String,
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        fixedParameterKinds: List<NamedMetricKind>,
        evaluator: AsyncBoundaryEvaluator,
        testDataEvaluator: AsyncBoundaryEvaluator,
        parameterSetListener: ParameterSetListener,
        boundaryDiameterFinishThreshold: Double,
        adoptEachValue: Boolean, // Whether to adopt each selected parameter value incrementally
    ): Mono<Map<NamedMetricKind, ParameterAdmissibleRanges>>

}