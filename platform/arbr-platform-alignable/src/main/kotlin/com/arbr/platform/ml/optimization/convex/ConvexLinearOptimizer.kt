package com.arbr.platform.ml.optimization.convex

import com.arbr.platform.ml.logging.LogUtils
import com.arbr.platform.ml.logging.LossTracker
import com.arbr.platform.ml.optimization.base.AsyncBoundaryEvaluator
import com.arbr.platform.ml.optimization.base.AsyncBoundaryOptimizer
import com.arbr.platform.ml.optimization.base.NamedMetricKind
import com.arbr.platform.ml.optimization.base.ParameterSetListener
import com.arbr.platform.ml.optimization.model.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import kotlin.math.max
import kotlin.math.roundToLong

class ConvexLinearOptimizer(
    private val paramMinValue: Double = 0.0,
    private val paramMaxValue: Double = 100.0,
) : AsyncBoundaryOptimizer {

    private val boundaryEstimator = ConvexLinearBoundaryEstimator(
        paramMinValue,
        paramMaxValue,
    )

    private fun passingIntervals(
        sample: List<Pair<Double, AsyncBoundarySimpleEvaluation>>,
        boundaryPoints: List<Double>
    ): List<ParameterIntervalResult> {
        fun majorityPassing(minValue: Double, maxValue: Double): Boolean {
            val intervalSample = sample
                .filter { it.first >= minValue && it.first < maxValue }
            return if (intervalSample.isEmpty()) {
                false
            } else {
                intervalSample.count { it.second.passed } * 1.0 / intervalSample.size >= 0.5
            }
        }

        val resultIntervals = mutableListOf<ParameterIntervalResult>()
        val sortedBoundaries = boundaryPoints.sorted()
        var intervalLeft = paramMinValue
        for (boundary in sortedBoundaries) {
            if (majorityPassing(intervalLeft, boundary)) {
                resultIntervals.add(
                    ParameterIntervalResult(intervalLeft, boundary)
                )
            }
            intervalLeft = boundary
        }
        if (majorityPassing(intervalLeft, paramMaxValue)) {
            resultIntervals.add(
                ParameterIntervalResult(intervalLeft, paramMaxValue)
            )
        }

        return resultIntervals
    }

    private fun findBoundaryAsync(
        parameterKind: NamedMetricKind,
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        evaluator: AsyncBoundaryEvaluator,
        numIterations: Int,
        boundaryEstimates: List<Double>,
        failedValues: List<Pair<Double, AsyncBoundarySimpleEvaluation>>,
        passedValues: List<Pair<Double, AsyncBoundarySimpleEvaluation>>,
        boundaryDiameterFinishThreshold: Double,
    ): Mono<ParameterAdmissibleRanges> {
        if (parameters.isEmpty() || numIterations <= 0) {
            return Mono.empty()
        }

        val bindingParameter = parameters[parameterKind]!!
        logger.info("")

        val isNew = failedValues.isEmpty() && passedValues.isEmpty()

        val testValues = if (isNew) {
            logger.info("Sourcing initial frontier")

            // Test initial and boundaries

            val paramValue = bindingParameter.value
            listOf(paramValue) // + (1..parallelism).map { i -> i * 1.0 / (1.0 + parallelism) } + listOf(0.0, 1.0)
        } else if (boundaryEstimates.isNotEmpty()) {
            logger.info("Sourcing boundary value frontier")

            boundaryEstimates
        } else {
            throw Exception("Nothing to test")
        }

        val quantizationUnit = 1E-5
        val noisedTestValues = testValues
            .map { (max(0.0, it) / quantizationUnit).roundToLong() }
            .distinct()
            .sorted()
            .map { it * quantizationUnit }

        logger.debug("Testing {}: {}", parameterKind, noisedTestValues)

        val parameterMaps = noisedTestValues.map { paramTestValue ->
            parameters.mapValues { (k, param) ->
                if (k == parameterKind) {
                    param.copy(value = paramTestValue)
                } else {
                    param
                }
            }
        }

        val failedPassedMono = evaluator.evaluateMany(parameterMaps, parameterKind)
            .map { (i, eval) ->
                val testedParameter = parameterMaps[i][parameterKind]!!
                testedParameter.value to eval
            }
            .doOnNext { (paramTestValue, eval) ->
                logger.info(
                    "Scored ${String.format("%.02f", eval.score)} using $parameterKind=${
                        String.format(
                            "%.06f",
                            paramTestValue
                        )
                    }"
                )
            }
            .collectList()

        return failedPassedMono
            .flatMap { evaluation ->
                val sample =
                    evaluation.map { it.first to AsyncBoundarySimpleEvaluation(it.second.passed, it.second.score) }

                val inputSample = passedValues + failedValues
                val combinedSample = inputSample + sample
                val bestEval = combinedSample.maxByOrNull { it.second.score }

                val newBoundaryEstimates = boundaryEstimator.computeBoundaryIteration(
                    inputSample,
                    evaluation,
                )

                logger.info(
                    "Sample $parameterKind: [${
                        combinedSample.sortedBy { it.first }
                            .joinToString(", ") {
                                String.format(
                                    "%.04f",
                                    it.first
                                ) + (if (it.second.passed) "T" else "F")
                            }
                    }]"
                )
                val maxDiameter = newBoundaryEstimates.mapNotNull { it.second }.maxOrNull()

                val didHitStopIteration = numIterations - 1 == 0
                if (didHitStopIteration || (maxDiameter != null && maxDiameter < boundaryDiameterFinishThreshold)) {
                    val convergedBoundaries = newBoundaryEstimates.mapNotNull { (boundaryEstimate, diameter) ->
                        if (diameter == null) {
                            null // Should not be interpreted directly as a result
                        } else {
                            boundaryEstimate
                        }
                    }

                    val intervals = passingIntervals(combinedSample, convergedBoundaries)
                    val admissibleRanges = if (bestEval == null) {
                        ParameterAdmissibleRanges(
                            bindingParameter,
                            0.0,
                            intervals,
                        )
                    } else {
                        ParameterAdmissibleRanges(
                            bindingParameter.copy(value = bestEval.first),
                            bestEval.second.score,
                            intervals,
                        )
                    }

                    if (didHitStopIteration) {
                        logger.info("Stopping with best score ${bestEval?.first}, intervals $intervals, diameter $maxDiameter")
                    } else {
                        logger.info("Finishing with best score ${bestEval?.first}, intervals $intervals, diameter $maxDiameter")
                    }

                    Mono.just(admissibleRanges)
                } else {
                    findBoundaryAsync(
                        parameterKind,
                        parameters,
                        evaluator,
                        numIterations - 1,
                        newBoundaryEstimates.map { it.first },
                        combinedSample.filter { !it.second.passed },
                        combinedSample.filter { it.second.passed },
                        boundaryDiameterFinishThreshold,
                    ).map { subAdmissibleRanges ->
                        subAdmissibleRanges
                    }
                }
            }
    }

    override fun optimizeBoundariesAsync(
        sessionName: String,
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        fixedParameterKinds: List<NamedMetricKind>,
        evaluator: AsyncBoundaryEvaluator,
        testDataEvaluator: AsyncBoundaryEvaluator,
        parameterSetListener: ParameterSetListener,
        boundaryDiameterFinishThreshold: Double,
        adoptEachValue: Boolean,
    ): Mono<Map<NamedMetricKind, ParameterAdmissibleRanges>> {
        if (parameters.isEmpty()) {
            return Mono.empty()
        }

        val initialRanges = parameters.mapValues { (_, p) -> ParameterAdmissibleRanges(p, null, emptyList()) }
        return (parameters.keys).fold(Mono.just(initialRanges)) { m, parameterKind ->
            if (parameterKind in fixedParameterKinds) {
                logger.info("Skipping fixed parameter at index $parameterKind: ${parameters[parameterKind]!!.metricKind.name}")
                m.map { parameterAdmissibleRangesMap ->
                    parameterAdmissibleRangesMap.mapValues { (kind, admissibleRanges) ->
                        if (kind == parameterKind) {
                            val paramValue = parameters[parameterKind]!!.value
                            admissibleRanges.copy(
                                ranges = listOf(
                                    ParameterIntervalResult(paramValue, paramValue) // Note technically empty interval
                                )
                            )
                        } else {
                            admissibleRanges
                        }
                    }
                }
            } else {
                m.flatMap { runningParameterAdmissibleRanges ->
                    val runningParameterValues = runningParameterAdmissibleRanges.mapValues { (_, p) -> p.parameter }
                    logger.info("Computing parameter boundary at index $parameterKind: ${parameters[parameterKind]!!.metricKind.name}")

                    findBoundaryAsync(
                        parameterKind,
                        runningParameterValues,
                        evaluator,
                        numIterations = 25,
                        boundaryEstimates = emptyList(),
                        failedValues = emptyList(),
                        passedValues = emptyList(),
                        boundaryDiameterFinishThreshold,
                    ).flatMap { parameterAdmissibleRanges ->
                        val bestScore = parameterAdmissibleRanges.score
                        LogUtils.setMDCLossContext(LossTracker.Source.CVXL, 1 - (bestScore ?: 0.0)) {
                            val newParamValue = parameterAdmissibleRanges.parameter.value

                            logger.info("Found boundary value for parameter $parameterKind: $newParamValue")

                            logger.info("Taking: $newParamValue")
                            logger.info("Score: $bestScore")

                            val newParamRanges = runningParameterAdmissibleRanges.mapValues { (kind, admissibleRanges) ->
                                if (kind == parameterKind) {
                                    if (adoptEachValue) {
                                        parameterAdmissibleRanges
                                    } else {
                                        admissibleRanges.copy(score = bestScore, ranges = parameterAdmissibleRanges.ranges)
                                    }
                                } else {
                                    admissibleRanges
                                }
                            }
                            val newParams = newParamRanges.mapValues { it.value.parameter }

                            logger.info("New params: (" + newParamRanges.values.joinToString(", ") {
                                String.format(
                                    "%.04f",
                                    it.parameter.value
                                )
                            } + ")")
                            logger.info("Running test set evaluation for $parameterKind")

                            testDataEvaluator.evaluate(newParams, parameterKind)
                                .flatMap { testEvaluation ->
                                    parameterSetListener.didEvaluateParameterSet(
                                        ScoredParameterSet(
                                            "${sessionName}_${parameterKind}",
                                            newParams,
                                            bestScore ?: 0.0,
                                            testEvaluation.score,
                                        ),
                                    )
                                }
                                .thenReturn(newParamRanges)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvexLinearOptimizer::class.java)
    }
}