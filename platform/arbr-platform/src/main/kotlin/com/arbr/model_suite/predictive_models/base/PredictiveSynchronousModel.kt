package com.arbr.model_suite.predictive_models.base

import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * A predictive model with simple synchronous inference.
 */
interface PredictiveSynchronousModel<U : Any, V : Any> : PredictiveModel<U, V> {

    val scheduler: Scheduler get() = Schedulers.boundedElastic()

    override val inputPredictionParallelism: Int
        get() = 1

    fun predictSynchronous(input: U): V?

    /**
     * Perform a plain prediction, possibly yielding empty or an error if unsuccessful.
     */
    override fun predict(input: U): Mono<V> {
        return Mono.defer {
            try {
                Mono.just(
                    Optional.ofNullable(predictSynchronous(input))
                )
            } catch (e: Exception) {
                Mono.error(e)
            }
        }
            .subscribeOn(scheduler)
            .flatMap { optValue ->
                Mono.justOrEmpty(optValue.getOrNull())
            }
    }

}