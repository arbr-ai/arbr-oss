package com.arbr.model_suite.predictive_models.base

import reactor.core.publisher.Mono

/**
 * Generic loader providing runtime context for loading a model.
 */
interface PredictiveModelLoader<PM: PredictiveModel<U, V>, U : Any, V : Any> {

    fun load(): Mono<PM>
}