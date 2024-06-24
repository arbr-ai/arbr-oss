package com.arbr.platform.ml.optimization.gradient

import com.arbr.platform.ml.logging.LogUtils
import com.arbr.platform.ml.logging.LossTracker
import com.arbr.platform.ml.optimization.base.GradientDescentEvaluator
import com.arbr.platform.ml.optimization.base.NamedMetricKind
import com.arbr.platform.ml.optimization.base.ParameterSetListener
import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.GradientDescentEvaluation
import com.arbr.platform.ml.optimization.model.ParameterAdmissibleRanges
import com.arbr.platform.ml.optimization.model.ScoredParameterSet
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class GradientDescentOptimizer(
    private val sessionName: String,
    private val parameterRanges: List<ParameterAdmissibleRanges>,
    private val evaluator: GradientDescentEvaluator,
    private val parameterSetListener: ParameterSetListener,
    private val paramMinValue: Double = 0.0,
    private val paramMaxValue: Double = 10.0,
    private val learningRate: Double = 1.0,
    private val tolerance: Double = 1e-8,
    private val maxIterations: Int = 1000,
) {
    private val initialParameters: Map<NamedMetricKind, BindingParameter<Double>> = parameterRanges
        .associate { it.parameter.metricKind to it.parameter }
    private val parameterValueIntervals = parameterRanges
        .associate { it.parameter.metricKind to it.ranges }

    private fun boundedValue(
        value: Double,
        kind: NamedMetricKind,
    ): Double {
        val intervals = parameterValueIntervals[kind]!!

        return intervals.firstNotNullOfOrNull { interval ->
            if (interval.minValue <= value && value < interval.maxValue) {
                value
            } else {
                null
            }
        } ?: run {
            min(paramMaxValue, max(paramMinValue, value))
        }
    }

    private data class GradientValue(
        val grad: Double,
        val advantage: Double,
        val delta: Double,
    )

    private fun computeGradient(
        runningParameters: Map<NamedMetricKind, BindingParameter<Double>>,
        currentLoss: Double,
        effectiveLearningRate: Double,
    ): Mono<Map<NamedMetricKind, GradientValue>> {
        return Flux.fromIterable(runningParameters.entries)
            .flatMap({ (k, param) ->
                val originalValue = param.value

                var epsilon = 0.001
                val plusValues = mutableListOf(originalValue)
                val minusValues = mutableListOf(originalValue)

                while (originalValue - epsilon >= paramMinValue && originalValue + epsilon <= paramMaxValue) {
                    plusValues.add(originalValue + epsilon)
                    minusValues.add(originalValue - epsilon)
                    epsilon *= 2
                }

                val plusParamMaps = plusValues.map { paramPlusValue ->
                    runningParameters.mapValues { (innerKind, bindingParameter) ->
                        if (k == innerKind) {
                            bindingParameter.copy(value = boundedValue(paramPlusValue, k))
                        } else {
                            bindingParameter
                        }
                    }
                }
                val plusEvalsFlux = evaluator.evaluateMany(plusParamMaps)
                    .map { (i, eval) ->
                        plusParamMaps[i][k]!!.value to eval
                    }

                val minusParamMaps = minusValues.map { paramMinusValue ->
                    runningParameters.mapValues { (innerKind, bindingParameter) ->
                        if (k == innerKind) {
                            bindingParameter.copy(value = boundedValue(paramMinusValue, k))
                        } else {
                            bindingParameter
                        }
                    }
                }
                val minusEvalsFlux = evaluator.evaluateMany(minusParamMaps)
                    .map { (i, eval) ->
                        minusParamMaps[i][k]!!.value to eval
                    }

                val bestValueMono = Flux.concat(
                    plusEvalsFlux,
                    minusEvalsFlux,
                )
                    .collectList()
                    .flatMap { valuesAndEvals ->
                        Mono.justOrEmpty<Pair<Double, GradientDescentEvaluation>>(
                            valuesAndEvals
                                .filter { (paramValue, eval) ->
                                    !eval.loss.isNaN() && !eval.loss.isInfinite() && paramValue != originalValue
                                }
                                .minByOrNull { (_, eval) ->
                                    eval.loss
                                }
                        )
                    }

                bestValueMono
                    .map { (paramValue, loss) ->
                        val delta = paramValue - originalValue
                        val advantage = currentLoss - loss.loss
                        val gradient = -advantage / delta

                        k to GradientValue(
                            gradient,
                            advantage,
                            delta,
                        )
                    }
            }, GRADIENT_DESCENT_PARALLELISM)
            .collectList()
            .map { indexedGradients ->
                val gradientMap = indexedGradients.associate { it.first to it.second }

                logger.info(
                    "Grads (${
                        gradientMap.entries.joinToString(", ") { (kind, d) ->
                            "${kind.name}=" + String.format(
                                "%.03f",
                                d.grad
                            )
                        }
                    })"
                )

                runningParameters.mapValues { (k, _) ->
                    gradientMap[k] ?: GradientValue(0.0, 0.0, 0.0)
                }
            }
    }

    private fun effectiveLearningRate(
        iterationDepth: Int,
    ): Double {
        val steps = 4
        val stepSize = maxIterations / steps
        val stepIndex = iterationDepth / stepSize
        val proportion = (steps - stepIndex) * 1.0 / steps
        return learningRate * proportion
    }

    private fun optimizeInner(
        parameterChoices: List<Map<NamedMetricKind, BindingParameter<Double>>>,
        lastLossValue: Double,
        iterationDepth: Int,
    ): Mono<Pair<Double, Map<NamedMetricKind, BindingParameter<Double>>>> {
        if (parameterChoices.isEmpty() || parameterChoices.size > 2) {
            return Mono.error(IllegalStateException())
        }

        if (iterationDepth >= maxIterations) {
            val firstParameters = parameterChoices.first()

            logger.info(
                "Ran out of iterations with loss $lastLossValue on params (${
                    firstParameters.entries.joinToString(", ") { (kind, param) ->
                        "${kind.name}=" + String.format(
                            "%.06f",
                            param.value
                        )
                    }
                })"
            )

            return parameterSetListener.didEvaluateParameterSet(
                ScoredParameterSet(
                    sessionName,
                    firstParameters,
                    1 - lastLossValue,
                    1 - lastLossValue,
                )
            ).thenReturn(lastLossValue to firstParameters)
        }

        val evaluationMono = Flux.fromIterable(parameterChoices)
            .flatMap({ parameters ->
                evaluator.evaluate(parameters).map { parameters to it }
            }, 2)
            .collectList()
            .map { parametersAndEvals ->
                parametersAndEvals.minBy { (parameters, eval) ->
                    eval.loss
                }
            }

        return evaluationMono.flatMap { (parameters, evaluation) ->
            val loss = evaluation.loss
            LogUtils.setMDCLossContext(LossTracker.Source.GRAD, loss) {
                val rewardDifference = abs(loss - lastLossValue)

                logger.info("===")
                logger.info(
                    "Loss ${
                        String.format(
                            "%.06f",
                            loss
                        )
                    } for (${
                        parameters.entries.joinToString(", ") { (kind, param) ->
                            "${kind.name}=" + String.format(
                                "%.03f",
                                param.value
                            )
                        }
                    })"
                )

                if (rewardDifference < tolerance) {
                    logger.info(
                        "Finishing with loss $loss, step norm ${rewardDifference}, on params (${
                            parameters.entries.joinToString(", ") { (kind, param) ->
                                "${kind.name}=" + String.format(
                                    "%.06f",
                                    param.value
                                )
                            }
                        })"
                    )

                    parameterSetListener.didEvaluateParameterSet(
                        ScoredParameterSet(
                            sessionName,
                            parameters,
                            1 - lastLossValue,
                            1 - lastLossValue,
                        )
                    ).thenReturn(loss to parameters)
                } else {
                    val effectiveLearningRate = effectiveLearningRate(iterationDepth)
                    val gradientMono = computeGradient(parameters, loss, effectiveLearningRate)
                    gradientMono.flatMap { gradient ->
                        val (targetKind, targetGradValue) = gradient.maxBy { (_, gv) -> gv.advantage }
                        logger.info("${targetKind.name} Value: ${parameters[targetKind]?.value}")
                        logger.info("${targetKind.name} Gradient: ${targetGradValue.grad}")
                        logger.info("${targetKind.name} Advantage: ${targetGradValue.advantage}")
                        logger.info("${targetKind.name} Leap Delta: ${targetGradValue.delta}")

                        val changeOptions = listOf(
                            abs(effectiveLearningRate * targetGradValue.grad) * targetGradValue.grad.sign,
                            abs(effectiveLearningRate * targetGradValue.delta) * targetGradValue.grad.sign,
                        ).filter { !it.isNaN() && !it.isInfinite() }

                        val nextParameterChoices = changeOptions.map { change ->
                            parameters.mapValues { (kind, param) ->
                                if (kind == targetKind) {
                                    val nextValue = boundedValue(param.value - change, kind)
                                    param.copy(value = nextValue)
                                } else {
                                    param
                                }
                            }
                        }

                        optimizeInner(
                            nextParameterChoices,
                            loss,
                            iterationDepth + 1
                        )
                    }
                }
            }
        }
    }

    fun optimize(): Mono<Map<NamedMetricKind, BindingParameter<Double>>> {
        return optimizeInner(
            listOf(initialParameters),
            Double.NEGATIVE_INFINITY,
            0,
        ).map { it.second }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GradientDescentOptimizer::class.java)

        private const val GRADIENT_DESCENT_PARALLELISM = 4
    }
}
