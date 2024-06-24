package com.arbr.platform.ml.optimization.grid

import com.arbr.platform.ml.logging.LogUtils
import com.arbr.platform.ml.logging.LossTracker
import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.ParameterAdmissibleRanges
import com.arbr.platform.ml.optimization.model.ParameterIntervalResult
import com.arbr.platform.ml.optimization.base.*
import com.arbr.platform.ml.optimization.base.AsyncEvaluator
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * Find an initial passing parameter set while making no assumptions about continuity.
 */
class InitialPassingValueFinder(
    private val passingThreshold: Double,
    private val valuesPerParameter: Int = 8,
    private val maxSearchEvaluations: Int = 25_000,
): AsyncBoundaryOptimizer {
    private val innerGridOptimizer = GridOptimizer(
        scale = 10.0,
        outwardSearch = false,
    )

    private class FoundPassingValueException(
        val passingParams: List<BindingParameter<Double>>,
        val passingScore: Double,
    ): Exception()

    override fun optimizeBoundariesAsync(
        sessionName: String,
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        fixedParameterKinds: List<NamedMetricKind>,
        evaluator: AsyncBoundaryEvaluator,
        testDataEvaluator: AsyncBoundaryEvaluator,
        parameterSetListener: ParameterSetListener,
        boundaryDiameterFinishThreshold: Double,
        adoptEachValue: Boolean
    ): Mono<Map<NamedMetricKind, ParameterAdmissibleRanges>> {
        val evaluationCounter = AtomicInteger()
        val newlyFound = AtomicBoolean(false)

        val multiEvaluator: (List<ParameterList>) -> Flux<Pair<Int, Double>> = { parameterLists ->
            val parameterMaps: List<Map<NamedMetricKind, BindingParameter<Double>>> = parameterLists.map { pl ->
                pl.associateBy { it.metricKind }
            }

            evaluator.evaluateMany(parameterMaps, null)
                .flatMap { (i, eval) ->
                    val params = parameterLists[i]

                    LogUtils.setMDCLossContext(LossTracker.Source.GRID, 1 - eval.score) {
                        val numEvals = evaluationCounter.incrementAndGet()
                        if (numEvals >= maxSearchEvaluations) {
                            val wasFound = newlyFound.getAndSet(true)
                            if (!wasFound) {
                                // TODO: Not maximal
                                logger.info("Initial grid search exhausted iterations; finishing with score ${eval.score} at $params")
                                Mono.error(
                                    FoundPassingValueException(
                                        params,
                                        eval.score
                                    )
                                )
                            } else {
                                Mono.just(i to eval.score)
                            }
                        } else if (eval.score >= passingThreshold) {
                            val wasFound = newlyFound.getAndSet(true)
                            if (!wasFound) {
                                logger.info("Initial grid search found passing score ${eval.score} at $params")
                                Mono.error(
                                    FoundPassingValueException(
                                        params,
                                        eval.score
                                    )
                                )
                            } else {
                                Mono.just(i to eval.score)
                            }
                        } else {
                            Mono.just(i to eval.score)
                        }
                    }
                }
        }

        val asyncEvaluator = AsyncEvaluator { parameterLists ->
            multiEvaluator(parameterLists)
        }

        val maxIterations = valuesPerParameter.toDouble().pow(parameters.size.toDouble()).toLong()

        return innerGridOptimizer.optimizeAsync(
            parameters.values.toList(),
            asyncEvaluator,
            asyncEvaluator,
            0.01,
            0.01,
            maxIterations,
        ) { Mono.empty() }
            .flatMap { resultParams ->
                multiEvaluator(listOf(resultParams)).next().map { (_, score) ->
                    resultParams to score
                }
            }
            .onErrorResume(FoundPassingValueException::class.java) { ex ->
                Mono.just(
                    ex.passingParams to ex.passingScore
                )
            }
            .map { (paramList, score) ->
                paramList.associateBy { p -> p.metricKind }
                    .mapValues { (_, param) ->
                        ParameterAdmissibleRanges(
                            param,
                            score,
                            listOf(ParameterIntervalResult(param.value, param.value)),
                        )
                    }
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InitialPassingValueFinder::class.java)
    }
}