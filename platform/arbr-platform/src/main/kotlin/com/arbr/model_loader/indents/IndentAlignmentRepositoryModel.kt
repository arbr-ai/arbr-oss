package com.arbr.model_loader.indents

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffOperation
import com.arbr.model_suite.predictive_models.linear_tree_indent.LinearTreeIndentPredictor
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentContentType
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class IndentAlignmentRepositoryModel(
    private val segmenterService: SegmenterService,
    private val contentTypeVocabularies: Map<SegmentContentType, List<String>>,
    val workflowHandleId: String,
    private val trainingScoreAdmissibleThreshold: Double,
) {

    private fun maskedContentType(contentType: SegmentContentType): SegmentContentType {
        return when (contentType) {
            SegmentContentType.JSX,
            SegmentContentType.JavaScript -> SegmentContentType.JavaScript
            SegmentContentType.TSX,
            SegmentContentType.TypeScript -> SegmentContentType.TypeScript

            SegmentContentType.HTML -> SegmentContentType.HTML
            SegmentContentType.CSS -> SegmentContentType.CSS
            SegmentContentType.Plaintext -> SegmentContentType.Plaintext
        }
    }

    private val isTraining: MutableList<AtomicBoolean> =
        MutableList(SegmentContentType.values().size) {
            AtomicBoolean()
        }

    private val contentTypePredictors: MutableList<LinearTreeIndentPredictor<Dim.VariableN>?> =
        MutableList(SegmentContentType.values().size) {
            null
        }

    // TODO: Find a less memory-intensive way of retraining online
    private val fileNamesAndContents = SegmentContentType.values().map {
        ConcurrentHashMap<String, String>()
    }

    /**
     * Content types needing re-training. Batched to avoid excessive training.
     * TODO: Something like a repo file state hash to validate whether models are up to date
     */
    private val dirtyKeys = ConcurrentHashMap<SegmentContentType, Unit>()

    private fun fileMapForContentType(contentType: SegmentContentType): ConcurrentHashMap<String, String> {
        return fileNamesAndContents[maskedContentType(contentType).ordinal]
    }

    @Synchronized
    private fun getPredictorForContentType(contentType: SegmentContentType): LinearTreeIndentPredictor<Dim.VariableN>? {
        return contentTypePredictors[maskedContentType(contentType).ordinal]
    }

    @Synchronized
    private fun setPredictorForContentType(
        contentType: SegmentContentType,
        predictor: LinearTreeIndentPredictor<Dim.VariableN>
    ) {
        contentTypePredictors[maskedContentType(contentType).ordinal] = predictor
    }

    private fun getSegmentationContentType(fileName: String): SegmentContentType {
        return maskedContentType(segmenterService.getBaseLanguage(fileName).first)
    }

    private fun retrain(contentType: SegmentContentType): Mono<Void> {
        val vocabList = contentTypeVocabularies[contentType]
            ?: return Mono.empty()

        val isTrainingAtomic = isTraining[contentType.ordinal]
        val didAcquire = isTrainingAtomic.compareAndSet(false, true)
        if (!didAcquire) {
            return Mono.empty()
        }

        // Remove the lock even before training actually begins, in case content is modified between now and training
        // and the content type is once again flagged dirty
        dirtyKeys.remove(contentType)

        return Mono.fromCallable {
            synchronized(this) {
                logger.info("Retraining indent model for $contentType")

                val fnc = fileMapForContentType(contentType)
                val pairList = fnc.toList()
                    .sortedBy { it.second.length }
                    .take(MAX_REPO_TRAINING_FILES)
                val predictor: LinearTreeIndentPredictor<Dim.VariableN> =
                    LinearTreeIndentPredictor.train<Dim.VariableN>(
                        segmenterService,
                        vocabList,
                        pairList,
                        testSampleSize = 0,
                        selfEvaluate = true,
                        numFeaturesShape = Dim.VariableN,
                    )
                val selfEvaluationScore = predictor.trainingStatistics.selfEvaluationScore
                if (selfEvaluationScore == null || selfEvaluationScore < trainingScoreAdmissibleThreshold) {
                    logger.warn("Discarding predictor on workflow $workflowHandleId for ${contentType.serializedName} with low score $selfEvaluationScore")
                } else {
                    setPredictorForContentType(contentType, predictor)
                }
            }
        }.subscribeOn(Schedulers.boundedElastic())
            .then()
            .doOnTerminate {
                isTrainingAtomic.set(false)
            }
            .doOnCancel {
                isTrainingAtomic.set(false)
            }
    }

    /**
     * Public interface
     */

    /**
     * Notify model of a file content update. Does not immediately trigger retraining.
     */
    @Synchronized
    fun fileContentWasUpdated(
        filePath: String,
        oldContent: String?,
        newContent: String?,
    ) {
        val filteredNewContent = newContent?.takeIf { it.length <= MAX_FILE_LENGTH }

        val contentType = getSegmentationContentType(filePath)
        val fileMap = fileMapForContentType(contentType)
        if (filteredNewContent == null) {
            if (fileMap.containsKey(filePath)) {
                dirtyKeys[contentType] = Unit
            }

            fileMap.remove(filePath)
        } else {
            if (filteredNewContent != fileMap[filePath]) {
                dirtyKeys[contentType] = Unit
            }

            fileMap[filePath] = filteredNewContent
        }
    }

    /**
     * Sequentially retrain any models that have changed data. The returned Mono completes once retraining is complete.
     */
    fun pollRetrainingIfNecessary(): Mono<Void> {
        val trainingContentTypes = dirtyKeys.keys().toList() // Remove only after acquiring lock
        return trainingContentTypes.fold(Mono.empty<Void>()) { m, trainingContentType ->
            m.then(
                retrain(trainingContentType)
            )
        }
    }

    /**
     * Get a formatter-compiler from an existing trained predictor corresponding to the given file name, if one exists.
     * Does not train a new predictor if one doesn't already exist.
     */
    @Synchronized
    fun getDocumentFormatterCompiler(
        fileName: String,
    ): TokenFormatterCompiler<DiffLiteralSourceDocument, DiffOperation>? {
        val contentType = getSegmentationContentType(fileName)
        val predictor = getPredictorForContentType(contentType)
            ?: return null
        return IndentTokenPretrainedFormatterCompiler(
            fileName,
            predictor,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IndentAlignmentRepositoryModel::class.java)

        private const val MAX_REPO_TRAINING_FILES = 20
        private const val MAX_FILE_LENGTH = 10000
    }
}