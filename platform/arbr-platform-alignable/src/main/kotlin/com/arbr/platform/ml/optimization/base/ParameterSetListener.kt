package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.ScoredParameterSet
import reactor.core.publisher.Mono

fun interface ParameterSetListener {

    fun didEvaluateParameterSet(
        parameterSet: ScoredParameterSet,
    ): Mono<Void>

}