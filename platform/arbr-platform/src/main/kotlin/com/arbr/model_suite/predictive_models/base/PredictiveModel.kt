package com.arbr.model_suite.predictive_models.base

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

data class PredictionResult<V : Any>(
    val result: V?,
    val error: Throwable?,
)

/**
 * A generic predictive model making inferences from an input type to an output type.
 */
interface PredictiveModel<U : Any, V : Any> {

    /**
     * Allowed parallelism of predictions across inputs for an instance of the model.
     */
    val inputPredictionParallelism: Int

    /**
     * Perform a plain prediction, possibly yielding empty or an error if unsuccessful.
     */
    fun predict(input: U): Mono<V>

    /**
     * Perform a plain prediction, wrapping results to always yield a value.
     */
    fun predictResult(input: U): Mono<PredictionResult<V>> {
        return predict(input)
            .map {
                PredictionResult(
                    it,
                    null,
                )
            }
            .switchIfEmpty {
                Mono.just(
                    PredictionResult(
                        null,
                        null,
                    )
                )
            }
            .onErrorResume {
                Mono.just(
                    PredictionResult(
                        null,
                        it,
                    )
                )
            }
    }

    /**
     * Predict for a batch of inputs and return alongside their index among the inputs, but not necessarily in order.
     * Default implementation: predict individually.
     */
    fun predictMany(inputs: List<U>): Flux<Pair<Int, PredictionResult<V>>> {
        return Flux.fromIterable(inputs.withIndex())
            .flatMap({ (i, value) ->
                predictResult(value)
                    .map { i to it }
            }, inputPredictionParallelism)
    }

    /**
     * Predict for a batch of inputs and return as a parallel list.
     */
    fun predictBatch(inputs: List<U>): Mono<List<PredictionResult<V>>> {
        return predictMany(inputs)
            .sort { (i0, _), (i1, _) ->
                i0 - i1
            }
            .map { it.second }
            .collectList()
    }

}

