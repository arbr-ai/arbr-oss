package com.arbr.model_loader.indents

import com.arbr.model_suite.predictive_models.linear_tree_indent.LinearTreeIndentPredictor
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import com.arbr.model_loader.indents.test_case.IndentPredictionOptimizationTestCase
import com.arbr.model_loader.indents.test_case.IndentPredictionOptimizationTestSuite
import com.arbr.util_common.collections.splitOn
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.reactor.single
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.ml.optimization.base.*
import com.arbr.ml.optimization.gradient.GradientDescentOptimizer
import com.arbr.ml.optimization.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.math.*

@Component
class IndentPredictionOptimizer(
    private val indentAlignmentService: IndentAlignmentService,
) {
    private val segmenterService: SegmenterService = SegmenterService()

    private fun indentFileParamKind(paramName: String) = NamedMetricKind(paramName)

    private fun selectTrainingFiles(
        allTrainingFiles: List<Pair<String, String>>,
    ): List<Pair<String, String>> {
        val fileLengths = allTrainingFiles.mapIndexed { i, (_, content) ->
            i to content.length
        }
            .filter { it.second <= MAX_FILE_LENGTH }
            .sortedBy { it.second }

        val medianSize = if (fileLengths.size % 2 == 1) {
            fileLengths[fileLengths.size / 2].second.toDouble()
        } else {
            (fileLengths[fileLengths.size / 2 - 1].second + fileLengths[fileLengths.size / 2].second) / 2.0
        }

        val minSize = medianSize * 0.75
        val maxSize = medianSize * 1.25
        val validFiles = fileLengths.filter { it.second >= minSize && it.second <= maxSize }

        if (validFiles.size <= MAX_REPO_TRAINING_FILES) {
            return validFiles.map { allTrainingFiles[it.first] }
        }

        val removeEachSide = ceil((validFiles.size - MAX_REPO_TRAINING_FILES) / 2.0).roundToInt()
        return validFiles
            .dropLast(removeEachSide)
            .drop(removeEachSide)
            .map {
                allTrainingFiles[it.first]
            }
    }

    private fun evalTestCaseInner(
        testSuite: IndentPredictionOptimizationTestSuite,
        testCase: IndentPredictionOptimizationTestCase,
        vocabList: List<String>,
        trainingFiles: List<Pair<String, String>>,
        targetFiles: List<Pair<String, String>>
    ): Double {
        if (trainingFiles.isEmpty() || targetFiles.isEmpty()) {
            return 0.0
        }

        val targetFileContent = targetFiles[0].second

        val selectedTrainingFiles = trainingFiles // selectTrainingFiles(trainingFiles)

        if (selectedTrainingFiles.isEmpty()) {
            return 0.0
        }

//        logger.info(
//            "Selected ${selectedTrainingFiles.size} files for training, avg. length ${
//                selectedTrainingFiles.map { it.second.length }.average().roundToInt()
//            }"
//        )

        // Add parameters here
        val predictor = try {
            LinearTreeIndentPredictor.train<Dim.VariableN>(
                segmenterService,
                vocabList,
                selectedTrainingFiles,
                testSampleSize = 0,
                selfEvaluate = false,
                numFeaturesShape = Dim.VariableN,
            )
        } catch (e: Exception) {
            return 0.0
        }

        // Add parameters here
        val sampler = predictor.compileDocument(
            testCase.testFileName,
            targetFileContent,
        )

        var totalError = 0.0
        var numLinesSampled = 0
        var numCorrect = 0
        for ((lineIndex, line) in sampler.documentModel.normalizedDocumentLines.withIndex()) {
            val expectedWhitespaceTotal = line.length - line.trimStart().length

            try {
                val predictedUnits = sampler.sample(lineIndex)
                val predictedWhitespaceTotal = predictedUnits * sampler.whitespaceUnit

                numLinesSampled++
                totalError += sqrt((predictedWhitespaceTotal - expectedWhitespaceTotal).absoluteValue.toDouble())
                if (predictedWhitespaceTotal == expectedWhitespaceTotal) {
                    numCorrect++
                }
            } catch (e: IllegalArgumentException) {
                // pass
            }
        }

        return if (numLinesSampled > 0) {
            val errorPerLine = totalError / numLinesSampled
            val successRate = numCorrect * 1.0 / numLinesSampled
            logger.info(
                "Test Suite: ${testSuite.id}  File: ${testCase.testFileName}  Error/Line: ${
                    String.format(
                        "%.06f",
                        errorPerLine
                    )
                }  Success Rate: ${String.format("%.06f", successRate)}"
            )

            successRate
        } else {
            0.0
        }
    }

    private val random = Random(27494123322L)

    private fun evalTestCaseInnerStochastic(
        testSuite: IndentPredictionOptimizationTestSuite,
        testCase: IndentPredictionOptimizationTestCase,
        vocabList: List<String>,
        trainingFiles: List<Pair<String, String>>,
        targetFiles: List<Pair<String, String>>,
        parameters: List<BindingParameter<Double>>,
    ): Mono<Double> {
        val numSamples = 32

        return Flux.fromIterable(0 until numSamples)
            .flatMap {
                val includedFileNames = parameters.mapNotNull { param ->
                    if (random.nextDouble() < param.value) {
                        param.metricKind.name
                    } else {
                        null
                    }
                }.toSet()
                val evalTrainingFiles = trainingFiles.filter { it.first in includedFileNames }

                Mono.fromCallable {
                    evalTestCaseInner(
                        testSuite,
                        testCase,
                        vocabList,
                        evalTrainingFiles,
                        targetFiles,
                    )
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .collectList()
            .map {
                it.sum() / numSamples
            }
    }

    private fun runOptimizationTestCase(
        testSuite: IndentPredictionOptimizationTestSuite,
        testCase: IndentPredictionOptimizationTestCase,
    ): Mono<Void> {
        val vocabListMono = indentAlignmentService.getVocabularyList(testCase.testFileName)
            .single("No vocabulary for ${testCase.testFileName}")

        return vocabListMono.flatMap { vocabList ->
            val repoFiles = testSuite.repoFiles.distinctBy { it.fileName }

            val (targetFiles, trainingFiles) = repoFiles.map {
                it.fileName to it.fileContent
            }
                .splitOn {
                    it.first == testCase.testFileName
                }

            Invariants.check {
                require(targetFiles.size == 1)
            }

            val goodFileSet = listOf(
                "App.jsx",
                "CarouselPage1.jsx",
                "EmbeddedVideo.jsx",
                "Footer.jsx",
                "NotificationBell.jsx",
                "RegistrationCodeEntry.jsx",
                "RequireAuth.jsx",
                "Toast.jsx",
                "index.jsx",
                "navLinks.jsx",
            )
            val params = trainingFiles.map { (fileName, _) ->
                BindingParameter<Double>(indentFileParamKind(fileName), if (fileName in goodFileSet) 0.9 else 0.1)
            }

            val numTrainingFiles = 10
            val trainingEvaluator = AsyncEvaluator { evalParamLists ->
                Flux.fromIterable(evalParamLists.withIndex())
                    .flatMap { (i, evalParams) ->
                        Mono.defer {
                            val includedFileNames = evalParams
                                .sortedByDescending { it.value - (it.metricKind.name.hashCode().absoluteValue % 100) / 10000.0 }
                                .take(numTrainingFiles)
//                            .filter { it.value >= 0.5 }
                                .map { it.metricKind.name }
                                .toSet()
                            val evalTrainingFiles = trainingFiles
                                .filter { it.first in includedFileNames }

                            evalTestCaseInnerStochastic(
                                testSuite,
                                testCase,
                                vocabList,
                                evalTrainingFiles,
                                targetFiles,
                                evalParams,
                            )
                        }
                            .map { score ->
                                i to score
                            }
                    }
            }

            // 0.96
            // App.jsx, CarouselPage1.jsx, EmbeddedVideo.jsx, Footer.jsx, NotificationBell.jsx, RegistrationCodeEntry.jsx, RequireAuth.jsx, Toast.jsx, index.jsx, navLinks.jsx
            val parameterSetListener = com.arbr.ml.optimization.base.ParameterSetListener { parameterSet ->
                val includedFileNames = parameterSet
                    .parameters
                    .entries
//                .filter { it.value.value >= 0.5 }
                    .sortedByDescending { it.value.value - (it.key.name.hashCode().absoluteValue % 100) / 10000.0 }
                    .take(numTrainingFiles)
                    .map { it.key.name }
                    .sorted()
                logger.info("Got parameter set ${parameterSet.name} with training_score=${parameterSet.trainingScore} and test_score=${parameterSet.testScore}")
                logger.info(
                    "Files [${parameterSet.testScore}]: ${
                        includedFileNames.joinToString(", ") {
                            Paths.get(it).last().toString()
                        }
                    }"
                )

                Mono.empty()
            }

            val scoreThreshold = 0.8
            val convexEval = com.arbr.ml.optimization.base.AsyncBoundaryEvaluator { parameterMaps, _ ->
                val parameterLists = parameterMaps
                    .map { parameterMap ->
                        params.map { parameterMap[it.metricKind]!! }
                    }

                trainingEvaluator.evaluateMany(
                    parameterLists,
                ).map { (i, score) ->
                    i to AsyncBoundaryEvaluation(
                        score >= scoreThreshold,
                        score,
                        emptyList()
                    )
                }
            }

            val gradEvaluator = GradientDescentEvaluator { parameterMaps ->
                val parameterLists = parameterMaps
                    .map { parameterMap ->
                        params.map { parameterMap[it.metricKind]!! }
                    }

                trainingEvaluator.evaluateMany(
                    parameterLists,
                ).map { (i, score) ->
                    i to GradientDescentEvaluation(
                        1 - score,
                    )
                }
            }

            val gradOpt = GradientDescentOptimizer(
                "session",
                params.map {
                    ParameterAdmissibleRanges(it, null, listOf(ParameterIntervalResult(0.0, 1.0)))
                },
                gradEvaluator,
                parameterSetListener,
                0.0,
                1.0,
            )

            gradOpt.optimize()
                .doOnNext {
                    println("Done:")
                    println(it)
                }
                .then()

//        return ConvexLinearOptimizer().optimizeBoundariesAsync(
//            "session",
//            params.associateBy { it.metricKind },
//            emptyList(),
//            convexEval,
//            convexEval,
//            parameterSetListener,
//            0.01,
//            true,
//        ).then()

//        return hillOpt.optimizeAsync(
//            params,
//            trainingEvaluator,
//            trainingEvaluator,
//            0.001,
//            0.001,
//            (2.0).pow(params.size).roundToLong(),
//            parameterSetListener,
//        ).then()
        }
    }

    private fun runOptimizationTestCase1File(
        testSuite: IndentPredictionOptimizationTestSuite,
        testCase: IndentPredictionOptimizationTestCase,
    ): Mono<Void> {
        val vocabListMono = indentAlignmentService.getVocabularyList(testCase.testFileName)
            .single("No vocabulary for ${testCase.testFileName}")

        return vocabListMono.flatMap { vocabList ->
            val repoFiles = testSuite.repoFiles.distinctBy { it.fileName }

            val (targetFiles, trainingFiles) = repoFiles.map {
                it.fileName to it.fileContent
            }
                .splitOn {
                    it.first == testCase.testFileName
                }

            Invariants.check {
                require(targetFiles.size == 1)
            }

            val orderedFileNames = trainingFiles.map { it.first }.sorted()
            val fileNameToContent = trainingFiles.toMap()
            val fileNameToIndex = orderedFileNames.withIndex()
                .associate { it.value to it.index }

            val pairwiseScores = MutableList(orderedFileNames.size) {
                MutableList(orderedFileNames.size) {
                    -1.0
                }
            }

            val fileScores = mutableMapOf<String, Double>()
            for ((i, p) in trainingFiles.sortedBy { it.first }.withIndex()) {
                val (trainFileName, trainFileContent) = p
                val evalTrainingFiles = listOf(trainFileName to trainFileContent)

                val score = evalTestCaseInner(
                    testSuite,
                    testCase,
                    vocabList,
                    evalTrainingFiles,
                    targetFiles,
                )

                logger.info("File $trainFileName: $score")

                fileScores[trainFileName] = score
                pairwiseScores[i][i] = -ln(1.0 - score)
            }

            for ((i, fileName) in orderedFileNames.withIndex()) {
                for ((j, otherFileName) in orderedFileNames.withIndex()) {
                    if (j <= i) {
                        continue
                    }

                    val fileContentI = fileNameToContent[fileName]!!
                    val fileContentJ = fileNameToContent[otherFileName]!!

                    val evalTrainingFiles = listOf(
                        fileName to fileContentI,
                        otherFileName to fileContentJ,
                    )

                    val score = evalTestCaseInner(
                        testSuite,
                        testCase,
                        vocabList,
                        evalTrainingFiles,
                        targetFiles,
                    )

                    logger.info("File $fileName x $otherFileName: $score")

                    val logScore = -ln(1.0 - score)
                    pairwiseScores[i][j] = logScore
                    pairwiseScores[j][i] = logScore
                }
            }

            jacksonObjectMapper().writeValue(File("pairwise.txt"), pairwiseScores)

//        val fileScores = mutableMapOf<String, Double>()
//        for ((trainFileName, trainFileContent) in trainingFiles.sortedBy { it.first }) {
//            val evalTrainingFiles = listOf(trainFileName to trainFileContent)
//
//            val score = evalTestCaseInner(
//                testSuite,
//                testCase,
//                vocabList,
//                evalTrainingFiles,
//                targetFiles,
//            )
//
//            logger.info("File $trainFileName: $score")
//
//            fileScores[trainFileName] = score
//        }
//
//        val sortedScores = fileScores.entries.sortedBy { it.value }
//        println("Best individual files:\n${sortedScores.joinToString("\n") { it.key + " : " + it.value.toString() }}")

            Mono.empty()
        }
    }

    private fun runOptimizationInterDep(
        testSuite: IndentPredictionOptimizationTestSuite,
    ): Mono<Void> {
        val vocabListMono = indentAlignmentService.getVocabularyList(testSuite.contentType)
            .single("No vocabulary for ${testSuite.contentType}")

        return vocabListMono.flatMap { vocabList ->
            val trainingFiles = testSuite.repoFiles.distinctBy { it.fileName }
                .sortedBy { it.fileName }

            val pairwiseScores = MutableList(trainingFiles.size) {
                MutableList(trainingFiles.size) {
                    -1.0
                }
            }
            val norms = MutableList(trainingFiles.size) { -1.0 }

            val indices = trainingFiles.indices.toList()
            val indexPairs = indices.flatMap { i -> indices.map { j -> i to j } }

            Flux.fromIterable(indexPairs)
                .flatMap { (i, j) ->
                    Mono.fromCallable {
                        val testCaseFile = trainingFiles[i]
                        val otherTestCaseFile = trainingFiles[j]

                        val fileContentI = testCaseFile.fileContent
                        val fileContentJ = otherTestCaseFile.fileContent

                        // Evaluate ability of i -> j prediction
                        val evalTrainingFiles = listOf(
                            testCaseFile.fileName to fileContentI,
                        )
                        val targetFiles = listOf(
                            otherTestCaseFile.fileName to fileContentJ,
                        )

                        val score = evalTestCaseInner(
                            testSuite,
                            IndentPredictionOptimizationTestCase(testSuite.id, otherTestCaseFile.fileName),
                            vocabList,
                            evalTrainingFiles,
                            targetFiles,
                        )
                        logger.info("File $i ${testCaseFile.fileName} -> $j ${otherTestCaseFile.fileName}: $score\n")

                        pairwiseScores[i][j] = score
                        if (i == j) {
                            norms[i] = score
                        }
                    }.subscribeOn(Schedulers.boundedElastic())
                }
                .collectList()
                .then(
                    Mono.fromCallable {
//                    val normalizedScores = pairwiseScores.indices.map { i ->
//                        val iNorm = norms[i]
//                        pairwiseScores[i].indices.map { j ->
//                            val score = pairwiseScores[i][j]
//                            val jNorm = norms[j]
//                            score / sqrt(iNorm * jNorm)
//                        }
//                    }

                        val mapper = jacksonObjectMapper()
                        val productDir = listOf(
                            Paths.get("/home/bill/share"),
                            Paths.get("/Users/bill/share"),
                        ).first { it.exists() && it.isDirectory() }
                        val paths = listOf(
                            Paths.get(""),
                            productDir,
                        )
                        for ((i, dirPath) in paths.withIndex()) {
                            mapper.writeValue(
                                Paths.get(dirPath.toString(), "pairwise_file_names.txt").toFile(),
                                trainingFiles.map { it.fileName })
                            mapper.writeValue(Paths.get(dirPath.toString(), "pairwise_s.txt").toFile(), pairwiseScores)
                        }
                    }.subscribeOn(Schedulers.boundedElastic())
                )
                .then()
        }
    }

    internal fun runOptimization(
        testSuite: IndentPredictionOptimizationTestSuite
    ): Mono<Void> {
//        return Flux.fromIterable(
//            testSuite.repoFiles.filter { it.fileName.contains("GitHubLogin.jsx") }
//        ).concatMap { testCaseFile ->
//            val testCase = IndentPredictionOptimizationTestCase(
//                testSuite.id,
//                testCaseFile.fileName,
//            )
//
//            runOptimizationTestCase(testSuite, testCase)
//        }
//            .collectList()
//            .then()

        return runOptimizationInterDep(
            testSuite
                .copy(repoFiles = testSuite.repoFiles.sortedBy { it.fileContent.length }.take(24))
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IndentPredictionOptimizer::class.java)

        private const val MAX_REPO_TRAINING_FILES = 20
        private const val MAX_FILE_LENGTH = 10000
    }
}
