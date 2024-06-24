package com.arbr.model_loader.training

import com.arbr.engine.app.config.ContextLifterConfiguration
import com.arbr.model_loader.loader.*
import com.arbr.model_loader.model.Dataset
import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.DiffPatchTrainingStep
import com.arbr.model_loader.training.config_model.TrainHardcodedConfiguration
import com.arbr.model_loader.training.config_model.TrainingConfiguration
import com.arbr.util_common.reactor.andPeriodically
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.arbr.platform.alignable.alignable.BASE_COST_METRIC_KINDS
import com.arbr.ml.logging.LogUtils
import com.arbr.ml.logging.LossTracker
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.base.ParameterSetListener
import com.arbr.ml.optimization.convex.ConvexLinearOptimizer
import com.arbr.ml.optimization.gradient.GradientDescentOptimizer
import com.arbr.ml.optimization.grid.InitialPassingValueFinder
import com.arbr.ml.optimization.model.BindingParameter
import com.arbr.ml.optimization.model.ScoredParameterSet
import org.apache.commons.lang3.time.DateFormatUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration
import java.util.*
import kotlin.system.exitProcess

@Component
class PatchEditDistanceOptimizer(
    private val parameterLoaderFactory: ParameterLoaderFactory,
    private val noisyRecordLoader: NoisyRecordLoader,
    private val hardcodedDiffDatasetLoader: HardcodedDiffDatasetLoader,
    private val simpleTextDiffDatasetLoader: SimpleTextDiffDatasetLoader,
    private val cleanDiffDatasetLoader: CleanDiffDatasetLoader,
) {

    private val noisyDiffDatasetLoaderV0 = NoisyDiffDatasetLoader(noisyRecordLoader, DiffPatchDatasetKind.SYNTHETIC_NOISE_V0)
    private val noisyDiffDatasetLoaderV1 = NoisyDiffDatasetLoader(noisyRecordLoader, DiffPatchDatasetKind.SYNTHETIC_NOISE_V1)
    private val noisyDiffDatasetLoaderV2 = NoisyDiffDatasetLoader(noisyRecordLoader, DiffPatchDatasetKind.SYNTHETIC_NOISE_V2)

    private val parameterLoaderMono = parameterLoaderFactory.makeLoader(
        production = false  // TODO: Source?
    )

    private val trainingConfiguration: TrainingConfiguration = TrainHardcodedConfiguration

    private val random = Random(483489321L)

    private val testCaseParallelism =
        System.getenv(ENV_VAR_TEST_CASE_PARALLELISM)?.toIntOrNull() ?: Runtime.getRuntime().availableProcessors()

    private var processMonoSink: MonoSink<Void>? = null
    private val processMono = Mono.create<Void> { sink ->
        processMonoSink = sink
    }

    private val costMetricKinds = BASE_COST_METRIC_KINDS

    private fun finish() {
        val sink = processMonoSink
        if (sink != null) {
            sink.success()
        } else {
            logger.warn("No process mono sink to trigger finish - shutting down aggressively")
            exitProcess(0)
        }
    }
    
    private fun loadParameterSet(): Mono<ScoredParameterSet> {
        return parameterLoaderMono.flatMap { 
            it.loadParameterSet()
        }
    }

    private fun persistParameterSet(
        parameterSet: ScoredParameterSet,
    ): Mono<Void> {
        return parameterLoaderMono.flatMap {
            it.persistParameterSet(parameterSet)
        }
    }

    private fun getStartingParameters(): Mono<Map<NamedMetricKind, BindingParameter<Double>>> {
        val missingParamDefaultValue = 1.234

        val paramsMono = loadParameterSet()
        return paramsMono.map { params ->
            logger.info(
                "Using initial parameters: (${
                    params.parameters.entries.joinToString(", ") { (kind, param) ->
                        "${kind.name}=" + String.format(
                            "%.06f",
                            param.value
                        )
                    }
                })"
            )
            logger.info("Recorded scores: train=${params.trainingScore} test=${params.testScore}")

            costMetricKinds.associateWith {
                params.parameters[it] ?: BindingParameter(it, missingParamDefaultValue)
            }
        }
    }

    private fun subsampleTrainingData(
        dataset: Dataset<DiffPatchTestCase>,
        subsampleRate: Double
    ): Mono<List<DiffPatchTestCase>> {
        return dataset.trainingData.collectList()
            .map { trainingData ->
                val indices =
                    trainingData.indices.shuffled(random).take((trainingData.size * subsampleRate).toInt())
                indices.map { trainingData[it] }
            }
    }

    private fun optimizeEpoch(
        dataset: Dataset<DiffPatchTestCase>,
        trainingStep: DiffPatchTrainingStep.Train,
        startingParams: Map<NamedMetricKind, BindingParameter<Double>>,
    ): Mono<Void> {
        val localDateNow = calendar.time
        val trainingEpochStartDateString = DateFormatUtils.format(
            localDateNow,
            "yyyy-MM-dd_HH-mm-ss"
        )

        val evaluator = TrainingEvaluator(dataset, trainingStep, testCaseParallelism)
        val testEvaluator = evaluator.asTestEvaluator()

        val baselineEvalMono = Flux.fromIterable(0 until 3)
            .concatMap {
                subsampleTrainingData(dataset, trainingStep.subsampleRate)
                    .flatMap { subsampledData ->
                        evaluator.evaluate(
                            canAddIgnored = true,
                            reposAndPatches = subsampledData,
                            parameterMap = startingParams,
                            targetParameterKind = null,
                        )
                    }
            }
            .flatMap { (score, thresholds) ->
                val loss = 1 - score
                LogUtils.setMDCLossContext(LossTracker.Source.INIT, loss) {
                    Mono.just(score to thresholds)
                }
            }
            .doOnSubscribe {
                logger.info("Performing 3x initial baseline evaluation")
            }
            .doOnNext { (score, _) ->
                logger.info("Baseline score: $score")
            }
            .then()

        val parameterSetListener = ParameterSetListener { parameterSet ->
            logger.info("Saving parameter set ${parameterSet.name} with training_score=${parameterSet.trainingScore} and test_score=${parameterSet.testScore}")
            persistParameterSet(parameterSet)
        }

        val initialPassingValueFinder = InitialPassingValueFinder(trainingStep.acceptThreshold, 1, 1)

        val sessionName = "${dataset.name}_${trainingEpochStartDateString}"
        return baselineEvalMono.then(
            Mono.defer {
                initialPassingValueFinder.optimizeBoundariesAsync(
                    sessionName,
                    startingParams,
                    emptyList(),
                    evaluator,
                    testEvaluator,
                    parameterSetListener,
                    trainingStep.boundaryDiameterFinishThreshold,
                    adoptEachValue = true,
                )
            }
                .flatMap { ranges ->
                    val nextParams = ranges.mapValues { (_, v) -> v.parameter }
                    ConvexLinearOptimizer().optimizeBoundariesAsync(
                        sessionName,
                        nextParams,
                        emptyList(),
                        evaluator,
                        testEvaluator,
                        parameterSetListener,
                        trainingStep.boundaryDiameterFinishThreshold,
                        adoptEachValue = true,
                    )
                }
                .flatMap { ranges ->
                    val nextParams = ranges.mapValues { (_, v) -> v.parameter }
                    ConvexLinearOptimizer().optimizeBoundariesAsync(
                        sessionName,
                        nextParams,
                        emptyList(),
                        evaluator,
                        testEvaluator,
                        parameterSetListener,
                        trainingStep.boundaryDiameterFinishThreshold,
                        adoptEachValue = false,
                    )
                }
                .doOnNext { ranges ->
                    for ((_, r) in ranges) {
                        logger.info(r.parameter.metricKind.name)
                        logger.info("  Value: ${r.parameter.value}")
                        logger.info("  Score: ${r.score}")
                        for (interval in r.ranges) {
                            logger.info("    (${interval.minValue}, ${interval.maxValue})")
                        }
                    }
                }
                .flatMap { ranges ->
                    GradientDescentOptimizer(
                        sessionName,
                        ranges.values.toList(),
                        evaluator,
                        parameterSetListener,
                        tolerance = GRADIENT_DESCENT_TOLERANCE,
                    ).optimize()
                }
        )
            .andPeriodically(Duration.ofSeconds(4L)) {
                evaluator.getTrainingThroughput()?.let { (evals, elapsedMs) ->
                    val rate = evals * 1000.0 / elapsedMs
                    logger.info(
                        "Training throughput ${String.format("%.03f", rate)} = $evals / ${elapsedMs / 1000L}s"
                    )
                }
            }
            .then()
    }

    private fun combineDataset(trainingStep: DiffPatchTrainingStep): Mono<Dataset<DiffPatchTestCase>> {
        val datasetMono = Flux.fromIterable(
            trainingStep.samples
        ).concatMap { sample ->
            val loader = when (sample.datasetKind) {
                DiffPatchDatasetKind.CLEAN -> cleanDiffDatasetLoader
                DiffPatchDatasetKind.SIMPLE_TEXT_DIFF -> simpleTextDiffDatasetLoader
                DiffPatchDatasetKind.HARDCODED -> hardcodedDiffDatasetLoader
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V0 -> noisyDiffDatasetLoaderV0
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V1 -> noisyDiffDatasetLoaderV1
                DiffPatchDatasetKind.SYNTHETIC_NOISE_V2 -> noisyDiffDatasetLoaderV2
            }

            Mono.fromCallable {
                loader.loadDataset(
                    sample.numTrainingFiles,
                    sample.numTestFiles,
                    trainingStep.subsampleRate,
                    trainingStep.dataLoadingRandomSeed,
                )
            }.subscribeOn(Schedulers.boundedElastic())
        }
            .collectList()
            .map { datasets ->
                Dataset(
                    datasets.joinToString("-") { it.name },
                    Flux.concat(datasets.map { it.trainingData }).share().cache(),
                    Flux.concat(datasets.map { it.testData }).share().cache(),
                )
            }
        return datasetMono
    }

    private fun evalOnlyTrainingStep(
        trainingStep: DiffPatchTrainingStep.Eval,
    ): Mono<Void> {
        val mapper = jacksonObjectMapper()
        val parameterSetsMono = Flux.fromIterable(
            listOf(
                loadParameterSet()
            )
        ).flatMap { it }.collectList()

        val datasetMono = combineDataset(trainingStep)

        return Mono.zip(
            datasetMono,
            parameterSetsMono,
        ).flatMap { (dataset, parameterSets) ->
            val evalTrainingEvaluator = TrainingEvaluator(dataset, trainingStep, 1)

            val parameterMaps = parameterSets.map {
                it.parameters
            }
            evalTrainingEvaluator
                .asTestEvaluator()
                .evaluateMany(parameterMaps, null)
                .doOnNext { (index, score) ->
                    val parameterSet = parameterSets[index]
                    logger.info("Evaluated parameter set: ${mapper.writeValueAsString(parameterSet)}")
                    logger.info("Eval score $index: $score")
                }
                .collectList()
                .map { pairs ->
                    pairs.maxBy { it.second.score }
                }
                .map { it.first to it.second.score }
                .flatMap { (i, bestScore) ->
                    logger.info("Saving best parameter set $i with score $bestScore")
                    persistParameterSet(parameterSets[i])
                }
        }

    }

    private fun optimizeTrainingStep(
        trainingStepIndex: Int,
        numTrainingSteps: Int,
        trainingStep: DiffPatchTrainingStep.Train,
    ): Mono<Void> {
        val datasetMono = combineDataset(trainingStep)

        return datasetMono
            .flatMapMany { dataset ->
                Flux.fromIterable(1..trainingStep.numEpochs)
                    .concatMap { epochNumber ->
                        getStartingParameters().flatMap { allParams ->
                            logger.info("\n\n==== Beginning Epoch $epochNumber of ${dataset.name} [Training Step ${trainingStepIndex + 1} / ${numTrainingSteps}] ====\n\n")
                            optimizeEpoch(dataset, trainingStep, allParams)
                        }
                    }
            }
            .then()
    }

    fun optimize() {
        val trainingSteps = trainingConfiguration.trainingSteps
        val numTrainingSteps = trainingSteps.size
        Flux.fromIterable(trainingSteps.withIndex())
            .concatMapDelayError { (i, trainingStep) ->
                LogUtils.setMDCLossContext(LossTracker.Source.INIT, 1.0) {
                    when (trainingStep) {
                        is DiffPatchTrainingStep.Eval -> evalOnlyTrainingStep(trainingStep)
                        is DiffPatchTrainingStep.Train -> optimizeTrainingStep(i, numTrainingSteps, trainingStep)
                    }
                }
            }
            .collectList()
            .doOnSubscribe {
                logger.info("Using parallelism: $testCaseParallelism")
            }
            .doOnError {
                logger.error("Error during optimization", it)
            }
            .doOnTerminate {
                finish()
            }
            .doOnCancel {
                finish()
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        Mono.using(
            { ContextLifterConfiguration.newInstance() },
            { processMono },
            { it.close() }
        ).block()

        logger.info("Optimization process finished")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PatchEditDistanceOptimizer::class.java)
        private val calendar = Calendar.getInstance(TimeZone.getTimeZone("US/Eastern"))

        private const val ENV_VAR_TEST_CASE_PARALLELISM = "ALIGNABLE_TEST_CASE_PARALLELISM"

        private const val GRADIENT_DESCENT_TOLERANCE = 1E-4
    }
}