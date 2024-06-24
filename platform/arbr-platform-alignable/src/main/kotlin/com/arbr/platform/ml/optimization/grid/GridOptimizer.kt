package com.arbr.platform.ml.optimization.grid

import com.arbr.platform.ml.optimization.base.AsyncEvaluator
import com.arbr.platform.ml.optimization.base.AsyncOptimizer
import com.arbr.platform.ml.optimization.base.Optimizer
import com.arbr.platform.ml.optimization.base.ParameterSetListener
import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.ScoredParameterSet
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class GridOptimizer(
    private val scale: Double = 1.0,
    private val outwardSearch: Boolean,
) : Optimizer, AsyncOptimizer {

    private fun coordinates(
        n: Int,
        d: Int
    ): Iterable<List<Int>> = Iterable {
        object : Iterator<List<Int>> {
            // The current state of the iterator represented as an array of integers.
            private val current = IntArray(n)

            // A flag to check if there are more elements to iterate over.
            private var hasNext = n > 0 && d > 0

            override fun hasNext(): Boolean = hasNext

            override fun next(): List<Int> {
                if (!hasNext) throw NoSuchElementException()

                // Capture the current state as the next value to return.
                val nextValue = current.toList()

                // Move to the next state.
                for (i in n - 1 downTo 0) {
                    if (current[i] < d - 1) {
                        current[i]++
                        break
                    } else {
                        current[i] = 0
                        if (i == 0) {
                            hasNext = false
                        }
                    }
                }

                return nextValue
            }
        }
    }

    private fun paramValues(
        parameters: List<BindingParameter<Double>>,
        gridSize: Int,
        scale: Double,
    ): Iterable<List<BindingParameter<Double>>> {
        val baseValues = parameters.map { it.value }

        return Iterable {
            val innerIterable = coordinates(parameters.size, gridSize)
            val innerIterator = innerIterable.iterator()

            object : Iterator<List<BindingParameter<Double>>> {
                override fun hasNext(): Boolean {
                    return innerIterator.hasNext()
                }

                override fun next(): List<BindingParameter<Double>> {
                    val innerNext = innerIterator.next()

                    return innerNext.mapIndexed { index, d ->
                        // 0 <= d <= gridSize - 1
                        val searchIndex = if (outwardSearch) {
                            d
                        } else {
                            gridSize - 1 - d
                        }

                        val epsilon = if (searchIndex % 2 == 0) {
                            ((searchIndex + 1) / 2) * (2.0 / gridSize)
                        } else {
                            -((searchIndex + 1) / 2) * (2.0 / gridSize)
                        }

                        var nextValue = baseValues[index] + epsilon * scale
                        if (nextValue < 0) {
                            nextValue = scale - abs(nextValue)
                        }

                        parameters[index].copy(value = nextValue)
                    }
                }
            }
        }
    }

    override fun optimize(
        parameters: List<BindingParameter<Double>>,
        eval: (List<BindingParameter<Double>>) -> Double,
        learningRate: Double,
        tolerance: Double,
        maxIterations: Int,
        destination: File?
    ): List<BindingParameter<Double>> {
        if (parameters.isEmpty() || maxIterations <= 0) {
            return parameters
        }

        val f = destination?.bufferedWriter()

        var bestParams: List<BindingParameter<Double>> = parameters
        var bestScore = -1.0

        val gridSize = (maxIterations.toDouble().pow(1.0 / parameters.size)).roundToInt()

        for ((iteration, paramList) in paramValues(parameters, gridSize, scale).withIndex()) {
            val score = eval(paramList)

            if (f != null) {
                val epochMs = Instant.now().toEpochMilli()
                f.append("Epoch $iteration @ $epochMs\n\n")
                f.append("Weights:\n")
                for (param in paramList) {
                    f.append("${param.value}\n")
                }
                f.append("\nReward:\n${score}\n\n")
                f.flush()
            }

            logger.info("Weights:")
            for (param in paramList) {
                logger.info("${param.value}")
            }
            logger.info("Reward:\n${score}")

            if (score > bestScore) {
                logger.info("New best!")
                bestScore = score
                bestParams = paramList
            }
        }

        return bestParams
    }

    override fun optimizeAsync(
        parameters: List<BindingParameter<Double>>,
        evaluator: AsyncEvaluator,
        testDataEvaluator: AsyncEvaluator,
        learningRate: Double,
        tolerance: Double,
        maxIterations: Long,
        parameterSetListener: ParameterSetListener,
    ): Mono<List<BindingParameter<Double>>> {
        if (parameters.isEmpty() || maxIterations <= 0) {
            return Mono.just(parameters)
        }

        val gridSize = (maxIterations.toDouble().pow(1.0 / parameters.size)).roundToInt()
        val scale = 1.0

        return Flux.fromIterable(paramValues(parameters, gridSize, scale))
            .buffer(MAX_BUFFER_SIZE)
            .concatMap { parameterLists ->
                evaluator.evaluateMany(parameterLists)
                    .collectList()
                    .mapNotNull<Pair<Int, Double>> { indexesAndScores ->
                        indexesAndScores.maxByOrNull { it.second }
                    }
                    .map { (i, score) ->
                        val params = parameterLists[i]
                        val parameterMap = params.associateBy { it.metricKind }
                        params to ScoredParameterSet(
                            name = "grid_${Instant.now().toEpochMilli()}",
                            parameters = parameterMap,
                            trainingScore = score,
                            testScore = 0.0,
                        )
                    }
                    .flatMap { (params, scoredParameterSet) ->
                        testDataEvaluator.evaluate(params).flatMap { testDataScore ->
                            val testScoredParamSet = scoredParameterSet.copy(testScore = testDataScore)

                            parameterSetListener.didEvaluateParameterSet(testScoredParamSet)
                                .thenReturn(testScoredParamSet)
                        }
                    }
            }
            .reduce { t, u ->
                if (t.trainingScore >= u.trainingScore) t else u
            }
            .flatMap {
                // Notify again with the best value
                parameterSetListener.didEvaluateParameterSet(it)
                    .thenReturn(it)
            }
            .map { scoredParameterSet ->
                val parameterMap = scoredParameterSet.parameters
                parameters.map { parameterMap[it.metricKind]!! }
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GridOptimizer::class.java)

        private const val MAX_BUFFER_SIZE = 16
    }
}