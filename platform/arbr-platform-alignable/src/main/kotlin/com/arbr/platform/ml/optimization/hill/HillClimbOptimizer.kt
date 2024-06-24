package com.arbr.platform.ml.optimization.hill

import com.arbr.platform.ml.optimization.base.AsyncEvaluator
import com.arbr.platform.ml.optimization.base.AsyncOptimizer
import com.arbr.platform.ml.optimization.base.Optimizer
import com.arbr.platform.ml.optimization.base.ParameterSetListener
import com.arbr.platform.ml.optimization.model.BindingParameter
import com.arbr.platform.ml.optimization.model.ScoredParameterSet
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.io.File
import java.time.Instant
import kotlin.math.max
import kotlin.random.Random

class HillClimbOptimizer: Optimizer, AsyncOptimizer {

    private val random = Random(79823153267)

    @Suppress("SameParameterValue")
    private fun generateCandidateSpread(
        parameters: List<BindingParameter<Double>>,
        batchSize: Int,
        maxValue: Double,
    ): List<List<Double>> {
        return (0 until batchSize).map {
            val parameterProportions = parameters.map { it.value / maxValue }

            val newValues = parameterProportions.map { p ->
                val range = max(p, 1 - p)
                var next: Double = -1.0

                while (next < 0.0 || next > 1.0) {
                    // Square random to simulate centered distribution
                    // in (-1, 1)
                    val dsq = (2 * random.nextDouble() - 1) * (1 - 2 * random.nextDouble())
                    next = p + dsq * range
                }

                next
            }

            newValues.map { x -> x * maxValue }
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

        val batchSize = 8
        val maxValue = 1.0
        var bestParams = parameters
        var bestScore = eval(parameters)
        logger.info("Init score: $bestScore with [" + parameters.joinToString { String.format("%.02f", it.value) } + "]")

        for (iteration in 0 until maxIterations) {
            if (f != null) {
                val epochMs = Instant.now().toEpochMilli()
                f.append("Epoch $iteration @ $epochMs\n\n")
                f.append("Weights:\n")
                for (param in bestParams) {
                    f.append("${param.value}\n")
                }
                f.append("\nReward:\n${bestScore}\n\n")
                f.flush()
            }


            for (valueList in generateCandidateSpread(bestParams, batchSize, maxValue)) {
                val paramList = valueList.zip(parameters).map { (v, p) -> p.copy(value = v) }
                val score = eval(paramList)
                logger.info("Batch score: $score with [" + valueList.joinToString { String.format("%.02f", it) } + "]")
                if (score > bestScore) {
                    logger.info("New best!")
                    bestScore = score
                    bestParams = parameters
                }
            }

            logger.info("Weights:")
            for (param in bestParams) {
                logger.info("${param.value}")
            }
            logger.info("Reward:\n${bestScore}")
        }

        return bestParams
    }

    private fun optimizeAsyncIteration(
        parameters: List<BindingParameter<Double>>,
        evaluator: AsyncEvaluator,
        testDataEvaluator: AsyncEvaluator,
        numIterations: Long,
        parameterSetListener: ParameterSetListener,
        bestScore: Double,
    ): Mono<List<BindingParameter<Double>>> {
        if (parameters.isEmpty() || numIterations <= 0) {
            return Mono.empty()
        }

        val batchSize = 8
        val maxValue = 1.0

        val parameterLists = generateCandidateSpread(parameters, batchSize, maxValue)
            .map {
                it.zip(parameters).map { (v, p) -> p.copy(value = v) }
            }

        return evaluator.evaluateMany(parameterLists)
            .collectList()
            .mapNotNull<Pair<Int, Double>> { indexesAndScores ->
                indexesAndScores.maxByOrNull { it.second }
            }
            .map { (i, score) ->
                val params = parameterLists[i]
                val parameterMap = params.associateBy { it.metricKind }
                params to ScoredParameterSet(
                    name = "hill_${Instant.now().toEpochMilli()}",
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
            .flatMap { scoredParameterSet ->
                if (scoredParameterSet.trainingScore >= bestScore) {
                    val parameterMap = scoredParameterSet.parameters
                    val nextParameters = parameters.map { parameterMap[it.metricKind]!! }
                    val nextBestScore = scoredParameterSet.trainingScore

                    optimizeAsyncIteration(
                        nextParameters,
                        evaluator,
                        testDataEvaluator,
                        numIterations - 1,
                        parameterSetListener,
                        nextBestScore,
                    )
                } else {
                    optimizeAsyncIteration(
                        parameters,
                        evaluator,
                        testDataEvaluator,
                        numIterations - 1,
                        parameterSetListener,
                        bestScore,
                    )
                }
            }
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
            return Mono.empty()
        }

        val initScore = evaluator.evaluate(parameters)

        return initScore.flatMap { score ->
            logger.info("Init score: $score with [" + parameters.joinToString { String.format("%.02f", it.value) } + "]")
            optimizeAsyncIteration(
                parameters,
                evaluator,
                testDataEvaluator,
                maxIterations,
                parameterSetListener,
                score,
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HillClimbOptimizer::class.java)
    }
}