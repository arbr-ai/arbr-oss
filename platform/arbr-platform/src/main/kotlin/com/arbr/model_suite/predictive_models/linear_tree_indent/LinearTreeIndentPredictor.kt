package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.arbr.model_suite.predictive_models.base.PredictiveSynchronousModel
import com.arbr.model_suite.predictive_models.linear_tree_indent.single_document.DocumentIndentSampler
import com.arbr.model_suite.predictive_models.linear_tree_indent.single_document.DocumentIndentSamplerImpl
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*
import kotlin.math.roundToInt

data class LinearTreeIndentPredictorInput(
    val filePath: String,
    val document: String,
)
data class LinearTreeIndentPredictorOutput(
    val filePath: String,
    val document: String,
)

class LinearTreeIndentPredictor<NumFeatures: Dim> private constructor(
    private val segmenterService: SegmenterService,
    private val encoder: DocumentIndentEncoder,
    private val combinedVocabulary: DocumentVocabulary,
    private val preTrainedWeights: ColumnVector<NumFeatures>,
    val trainingStatistics: TrainingStatistics,
): PredictiveSynchronousModel<String, String> {
    override val inputPredictionParallelism: Int = 1

    data class TrainingStatistics(
        val selfEvaluationScore: Double?,
        val testDataScore: Double?,
    )

    fun compileDocument(
        filePath: String,
        document: String,
    ): DocumentIndentSampler {
        if (!segmenterService.segmenterIsAvailableForFile(filePath)) {
            throw Exception("Document named $filePath does not appear to be segmentable")
        }

        val documentModel = DocumentModel(segmenterService, combinedVocabulary, filePath, document)
        val encoder = DocumentIndentEncoderImpl(combinedVocabulary)
        val featureDimensionsShape = preTrainedWeights.numRowsShape
        val sample = encoder.encode<Dim.VariableM, NumFeatures>(documentModel, Dim.VariableM, featureDimensionsShape)
        val documentWeights = TypedMatrices.linearRegression(sample)

        return DocumentIndentSamplerImpl(
            encoder,
            preTrainedWeights,
            documentWeights,
            documentModel,
            featureDimensionsShape,
        )
    }

    override fun predictSynchronous(input: String): String? {
        TODO("Not yet implemented")
    }

    companion object {
        private val random = Random(9812981231L)

        private val logger: Logger by lazy {
            LoggerFactory.getLogger(LinearTreeIndentPredictor::class.java)
        }

        private fun <SampleSize: Dim, NumFeatures: Dim> eval(
            sample: TypedMatrices.Sample<SampleSize, NumFeatures>,
            weights: ColumnVector<NumFeatures>,
        ): Double {
            val evalPredictions = sample.inputs.mult(weights)
            val predictions = evalPredictions.asArray()
                .map { it.first() }
            val actual = sample.outputs.asArray()
                .map { it.first() }
            val outcomes = predictions.zip(actual).map { (p, q) ->
                p.roundToInt() == q.roundToInt()
            }
            logger.info("Outcomes: ${outcomes.count { it }} / ${outcomes.size}")
            val successRate = outcomes.count { it } * 1.0 / outcomes.size
            logger.info("Success rate: $successRate")

            return successRate
        }

        fun <NumFeatures: Dim> train(
            segmenterService: SegmenterService,
            vocabList: List<String>,
            filePathsAndDocuments: List<Pair<String, String>>,
            testSampleSize: Int,
            selfEvaluate: Boolean,
            numFeaturesShape: NumFeatures,
        ): LinearTreeIndentPredictor<NumFeatures> {
            val sampleSizeShape = Dim.VariableM
            val documentSampleSizeShape = Dim.VariableD

            val commonVocabulary = DocumentVocabulary(vocabList, 1)

            val documentModels = filePathsAndDocuments.map { (filePath, document) ->
                if (!segmenterService.segmenterIsAvailableForFile(filePath)) {
                    throw Exception("Document named $filePath does not appear to be segmentable")
                }

                DocumentModel(segmenterService, commonVocabulary, filePath, document)
            }
            val encoder = DocumentIndentEncoderImpl(
                commonVocabulary,
            )

            val combinedSample = encoder.encodeMany(documentModels, sampleSizeShape,
                documentSampleSizeShape, numFeaturesShape)
            val combinedSampleSize = combinedSample.inputs.numRows
            logger.info("Combined sample size of ${filePathsAndDocuments.size} documents: $combinedSampleSize x ${combinedSample.inputs.numColumns} + 1")

            val (trainSample, testSample) = if (testSampleSize > 0) {
//                Invariants.check { require ->
//                    require(testSampleSize < combinedSampleSize)
//                }
                val splitSample = TypedMatrices.trainTestSplitByCount(
                    combinedSample,
                    combinedSampleSize - testSampleSize,
                    testSampleSize,
                    random,
                )
                splitSample.train to splitSample.test
            } else {
                combinedSample to null
            }

            logger.info("Training on sample size: ${trainSample.inputs.numRows}; ${testSample?.inputs?.numRows ?: 0} reserved for test")
            val weights = TypedMatrices.linearRegression(trainSample)

            val selfEvaluationScore = if (selfEvaluate) {
                logger.info("Self-evaluating...")
                eval(trainSample, weights)
            } else {
                null
            }
            val testDataScore = if (testSample != null) {
                logger.info("Evaluating test set...")
                eval(testSample, weights)
            } else {
                null
            }
            val trainingStatistics = TrainingStatistics(selfEvaluationScore, testDataScore)

            return LinearTreeIndentPredictor(segmenterService, encoder, commonVocabulary, weights, trainingStatistics)
        }

        fun <NumFeatures: Dim> train(
            segmenterService: SegmenterService,
            vocabFilePath: Path,
            filePathsAndDocuments: List<Pair<String, String>>,
            testSampleSize: Int,
            selfEvaluate: Boolean,
            numFeaturesShape: NumFeatures,
        ): LinearTreeIndentPredictor<NumFeatures> {
            val vocabList = vocabFilePath.toFile().readText().lines()
            return train(segmenterService, vocabList, filePathsAndDocuments, testSampleSize, selfEvaluate, numFeaturesShape)
        }

        fun <NumFeatures: Dim> compileSingleDocument(
            segmenterService: SegmenterService,
            vocabulary: List<String>,
            fileName: String,
            document: String,
            numFeaturesShape: NumFeatures,
        ): DocumentIndentSampler {
            if (!segmenterService.segmenterIsAvailableForFile(fileName)) {
                throw Exception("Document named $fileName does not appear to be segmentable")
            }

            val documentVocabulary = DocumentVocabulary(vocabulary, 1)
            val documentModel = DocumentModel(segmenterService, documentVocabulary, fileName, document)
            val encoder = DocumentIndentEncoderImpl(documentVocabulary)
            val sample = encoder.encode<Dim.VariableM, NumFeatures>(documentModel, Dim.VariableM, numFeaturesShape)
            val weights = TypedMatrices.linearRegression(sample)

            return DocumentIndentSamplerImpl(
                encoder,
                weights,
                weights,
                documentModel,
                numFeaturesShape,
            )
        }
    }
}