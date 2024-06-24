package com.arbr.model_loader.training

import com.arbr.content_formats.format.DiffLiteralSourceDocumentSerializer
import com.arbr.model_loader.model.Dataset
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.DiffPatchTrainingStep
import com.arbr.model_loader.model.EvaluationExhaustedTimeoutsException
import com.arbr.model_suite.parameters.ParameterValueProviderImpl
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelperImpl
import com.arbr.util_common.reactor.andPeriodically
import com.arbr.alignable.util.OperationLimitException
import com.arbr.ml.optimization.base.AsyncBoundaryEvaluator
import com.arbr.ml.optimization.base.GradientDescentEvaluator
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.model.AsyncBoundaryEvaluation
import com.arbr.ml.optimization.model.BindingParameter
import com.arbr.ml.optimization.model.GradientDescentEvaluation
import org.apache.commons.text.similarity.LevenshteinDistance
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.absoluteValue
import kotlin.math.exp

class TrainingEvaluator(
    private val dataset: Dataset<DiffPatchTestCase>,
    private val trainingStep: DiffPatchTrainingStep,
    private val parallelism: Int,
) : AsyncBoundaryEvaluator, GradientDescentEvaluator {
    private val ignoreIndices = ConcurrentLinkedDeque<Int>()
    private val levenshteinDistanceInstance = LevenshteinDistance()
    private val random = Random(483489321L)

    private val evaluationCounter = AtomicLong()
    private var trainingStartTime: Long? = null

    private val acceptThreshold = when (trainingStep) {
        is DiffPatchTrainingStep.Eval -> 0.99
        is DiffPatchTrainingStep.Train -> trainingStep.acceptThreshold
    }

    @Synchronized
    private fun setTrainingStartTime() {
        if (trainingStartTime == null) {
            trainingStartTime = Instant.now().toEpochMilli()
        }
    }

    /**
     * Get throughput as (evals, elapsedMs)
     */
    @Synchronized
    fun getTrainingThroughput(): Pair<Long, Long>? {
        val startMs = trainingStartTime ?: return null
        val elapsedMs = Instant.now().toEpochMilli() - startMs
        val evalCount = evaluationCounter.get()
        return evalCount to elapsedMs
    }

    private fun subsampleTrainingData(): Mono<List<DiffPatchTestCase>> {
        return dataset.trainingData.collectList()
            .map { trainingData ->
                val indices =
                    trainingData.indices.shuffled(random).take((trainingData.size * trainingStep.subsampleRate).toInt())
                indices.map { trainingData[it] }
            }
    }

    private fun regularize(
        parameters: Map<NamedMetricKind, BindingParameter<Double>>,
        score: Double,
    ): Double {
        if (parameters.isEmpty()) {
            return score
        }

        val l1Norm = parameters.values.sumOf { (100.0 - it.value).absoluteValue } / parameters.size
        val alpha = 0.01
        val multiplier = 1 / (1 + alpha * l1Norm)

        val regularWeight = 0.01
        return (1 - regularWeight) * score + regularWeight * multiplier
    }

    fun evaluate(
        canAddIgnored: Boolean,
        reposAndPatches: List<DiffPatchTestCase>,
        parameterMap: Map<NamedMetricKind, BindingParameter<Double>>,
        targetParameterKind: NamedMetricKind?,
    ): Mono<Pair<Double, List<Double>>> {
        val parameterValueProvider = ParameterValueProviderImpl(parameterMap)

        val allowedTimeoutsRemaining = AtomicInteger(10)

        // Use a much lower timeout for the initial pass where we can exclude test cases
        val alignmentTimeout = if (canAddIgnored) {
            Duration.ofSeconds(15L)
        } else {
            Duration.ofSeconds(30L)
        }

        val numTestCases = reposAndPatches.size - ignoreIndices.size

        if (targetParameterKind != null) {
            val targetParameter = parameterMap[targetParameterKind]!!
            logger.debug(
                "Running ${targetParameter.metricKind.name}=${
                    String.format(
                        "%.06f",
                        targetParameter.value
                    )
                } over $numTestCases test cases"
            )
        }

        val numCompleted = AtomicInteger()

        return Flux.fromIterable(reposAndPatches)
            .index()
            .flatMap({ t ->
                val helper = DocumentDiffAlignmentHelperImpl(
                    parameterValueProvider,
                )

                val index = t.t1.toInt()
                val testCase = t.t2

                if (index in ignoreIndices) {
                    Mono.empty()
                } else {
                    val expectedResult = testCase.expectedResult
                    val patch = testCase.patch
                    val baseDocument = testCase.baseDocument

                    try {
                        val alignedDocumentMono = helper.alignBySectionAsync(
                            patch,
                            baseDocument,
                            innerTimeout = alignmentTimeout,
                        )
                            .timed()

                        Mono.defer {
                            alignedDocumentMono
                        }
                            .single()
                            .map { alignedDocumentStateTimed ->
                                val alignedDocumentState = alignedDocumentStateTimed.get()
                                // val elapsedMs = alignedDocumentStateTimed.elapsed().toMillis()

                                val timeScoreMult = 1.0 // exp(-elapsedMs / 5000.0)

                                val serializedResult =
                                    DiffLiteralSourceDocumentSerializer().serialize(
                                        alignedDocumentState
                                            .map { it.diffOperation }
                                    )

                                val levenshteinDistance =
                                    levenshteinDistanceInstance.apply(expectedResult.text, serializedResult.text)

                                val distMultiplier = exp(-1.0 * levenshteinDistance / 100.0)
                                val score = distMultiplier * timeScoreMult

//                                logger.info("Score on ${testCase.name}: $score")
//                                if (numCompleted.get() % 10 == 0) {
//                                    logger.info("Score on ${testCase.name}: $score")
//                                }

                                score
                            }
                            .onErrorResume {
                                if (it is TimeoutException) {
                                    if (canAddIgnored) {
                                        // Remove cases that time out with the initial weights since they're unlikely to be
                                        // fixed, but penalize novel timeouts that come from weight changes
                                        logger.info("Timed out on $index - ignoring")
                                        ignoreIndices.add(index)

                                        Mono.empty()
                                    } else {
                                        val remainingTimeouts = allowedTimeoutsRemaining.decrementAndGet()
                                        if (remainingTimeouts > 0) {
                                            logger.info("Timed out on $index (${expectedResult.text.length}, ${patch.text.length}) - $remainingTimeouts left before fail")
                                            Mono.just(0.0)
                                        } else if (remainingTimeouts == 0) {
                                            logger.info("Exceeded allowed timeouts - scoring 0.0")
                                            Mono.error(EvaluationExhaustedTimeoutsException())
                                        } else {
                                            logger.info("Timeouts already exhausted")
                                            Mono.empty()
                                        }
                                    }
                                } else if (it is OperationLimitException) {
                                    // Give some credit to incentivize accurate cost bounds over waiting forever
                                    // logger.info("Awarding partial score for truncating execution early (${expectedResult.text.length}, ${patch.text.length})")
                                    if (canAddIgnored) {
                                        logger.info("Op limit exceeded on $index - ignoring")
                                        ignoreIndices.add(index)
                                    }
                                    Mono.just(0.0)
                                } else {
                                    logger.error("Unknown exception - ignoring", it)
                                    Mono.just(0.0)
                                }
                            }
                    } catch (e: Exception) {
                        logger.info("Got exception ${e.message}")
                        Mono.just(0.0)
                    }
                        .onErrorResume {
                            logger.error("Inner error ${it::class.simpleName}")
                            Mono.error(it)
                        }
                }
            }, parallelism)
            .doOnNext {
                numCompleted.incrementAndGet()
            }
            .onErrorResume {
                logger.error("Flux error ${it::class.simpleName}")
                Mono.error(it)
            }
            .collectList()
            .onErrorResume {
                logger.error("Outer error ${it::class.simpleName}")
                Mono.error(it)
            }
            .onErrorResume(EvaluationExhaustedTimeoutsException::class.java) {
                logger.info("Exceeded allowed timeouts - scoring 0.0")
                Mono.just(listOf(0.0))
            }
            .andPeriodically(Duration.ofSeconds(60L)) { i ->
                logger.info("[$i] completed ${numCompleted.get()} / $numTestCases")
            }
            .map<Pair<Double, List<Double>>?> { scores ->
                val finalScore = if (scores.isEmpty()) {
                    0.0
                } else {
                    val baseScore = scores.sum() / scores.size
                    regularize(parameterMap, baseScore)
                }

                evaluationCounter.addAndGet(numCompleted.get().toLong())

                finalScore to emptyList()
            }
            .doOnSubscribe {
                setTrainingStartTime()
            }
    }

    override fun evaluateMany(
        parameterMaps: List<Map<NamedMetricKind, BindingParameter<Double>>>,
        targetParameterKind: NamedMetricKind?
    ): Flux<Pair<Int, AsyncBoundaryEvaluation>> {
        return subsampleTrainingData().flatMapMany { subsample ->
            Flux.fromIterable(parameterMaps.withIndex())
                .concatMap { (idx, paramMap) ->
                    evaluate(
                        canAddIgnored = false,
                        reposAndPatches = subsample,
                        parameterMap = paramMap,
                        targetParameterKind = targetParameterKind,
                    ).map { (score, thresholds) ->
                        idx to AsyncBoundaryEvaluation(score > acceptThreshold, score, thresholds)
                    }
                }
        }
    }

    override fun evaluateMany(parameterMaps: List<Map<NamedMetricKind, BindingParameter<Double>>>): Flux<Pair<Int, GradientDescentEvaluation>> {
        return subsampleTrainingData().flatMapMany { subsample ->
            Flux.fromIterable(parameterMaps.withIndex())
                .concatMap { (idx, paramMap) ->
                    evaluate(
                        canAddIgnored = false,
                        reposAndPatches = subsample,
                        parameterMap = paramMap,
                        targetParameterKind = null,
                    ).map { (score, _) ->
                        idx to GradientDescentEvaluation(1 - score)
                    }
                }
        }
    }

    fun asTestEvaluator(): AsyncBoundaryEvaluator = AsyncBoundaryEvaluator { parameterMaps, targetParameterKind ->
        Flux.fromIterable(parameterMaps.withIndex())
            .concatMap { (idx, paramMap) ->
                dataset.testData.collectList().flatMap { testData ->
                    evaluate(
                        canAddIgnored = false,
                        reposAndPatches = testData, // Do not subsample test data, so evals are consistent
                        parameterMap = paramMap,
                        targetParameterKind = targetParameterKind,
                    ).map { (score, thresholds) ->
                        logger.info("Test Data Score: $score")
                        idx to AsyncBoundaryEvaluation(score > acceptThreshold, score, thresholds)
                    }
                }
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TrainingEvaluator::class.java)
    }
}