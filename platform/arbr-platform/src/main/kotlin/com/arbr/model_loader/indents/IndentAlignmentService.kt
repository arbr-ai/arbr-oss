package com.arbr.model_loader.indents

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffOperation
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.data_common.base.pipeline.DataPipelineBuilder
import com.arbr.model_loader.loader.config.PlaintextMapDataLoader
import com.arbr.model_loader.model.LanguageVocabularyWords
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentContentType
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import jakarta.annotation.PostConstruct
import com.arbr.ml.optimization.model.BindingParameter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI
import java.nio.file.Paths
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readLines

@Component
class IndentAlignmentService(
    @Qualifier("documentModelVocabularyDataExtractor")
    private val documentModelVocabularyDataExtractorMono: Mono<DataExtractor<LanguageVocabularyWords, RecordGrouping.Single>>,
    @Qualifier("plaintextVocabularyMapDataLoader")
    private val plaintextVocabularyMapDataLoader: PlaintextMapDataLoader<LanguageVocabularyWords, RecordGrouping.Single>,

    @Qualifier(IndentAlignmentConfiguration.TRAINING_SCORE_ADMISSIBLE_THRESHOLD_NAME)
    private val trainingScoreAdmissibleThreshold: BindingParameter<Double>,
) {
    private val contentTypeVocabulariesMono = Mono.defer {
        documentModelVocabularyDataExtractorMono
            .flatMap { dataExtractor ->
                val dataPipeline = DataPipelineBuilder()
                    .withExtractor(dataExtractor)
                    .withLoader(plaintextVocabularyMapDataLoader)
                    .build()

                dataPipeline
                    .load()
                    .then(
                        Mono.defer {
                            plaintextVocabularyMapDataLoader.getTextMap()
                        }
                    )
            }
            .map { stringMap ->
                val fileNameToVocabLines = stringMap.map { (uriModel, vocabText) ->
                    uriModel.lenientFileName() to vocabText.split("\n")
                }.toMap()

                contentTypeVocabFileNames.mapValues { (contentType, fileName) ->
                    fileNameToVocabLines[fileName]
                        ?: throw NoSuchElementException("No vocab entry for: $contentType, $fileName")
                }
            }
    }.cache()

    private val segmenterService: SegmenterService = SegmenterService()

    private val contentTypeVocabFileNames: Map<SegmentContentType, String> = mapOf(
        SegmentContentType.CSS to "css.txt",
        SegmentContentType.HTML to "html.txt",
        SegmentContentType.JavaScript to "javascript.txt",
        SegmentContentType.JSX to "javascript.txt",
    )

    // TODO: Prune when workflow is finished
    private val repoModelMap = ConcurrentHashMap<String, IndentAlignmentRepositoryModel>()

    private var contentTypeVocabulariesLoaded: Map<SegmentContentType, List<String>>? = null

    @Synchronized
    private fun setVocabMap(vocabMap: Map<SegmentContentType, List<String>>) {
        if (contentTypeVocabulariesLoaded == null) {
            contentTypeVocabulariesLoaded = vocabMap
        }
    }

    @Synchronized
    private fun getVocabMap(): Map<SegmentContentType, List<String>>? {
        return contentTypeVocabulariesLoaded
    }

    @PostConstruct
    fun init() {
        // Start loading immediately
        contentTypeVocabulariesMono
            .doOnNext { vocabMap ->
                setVocabMap(vocabMap)
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun innerRetrainModelsIfNecessary(): Mono<Void> {
        val repoModels = repoModelMap.values.toList()
        return repoModels.fold(Mono.empty<Void>()) { m, repoModel ->
            m.then(
                repoModel.pollRetrainingIfNecessary()
            )
        }
    }

    @Scheduled(fixedRate = 10103L)
    fun retrainModelsIfNecessary() {
        innerRetrainModelsIfNecessary()
            .subscribeOn(Schedulers.single()) // TODO: Enqueue on a Flux processor or similar
            .subscribe()
    }

    fun getVocabularyList(
        contentType: SegmentContentType,
    ): Mono<List<String>> {
        return contentTypeVocabulariesMono
            .mapNotNull { vocabMap ->
                vocabMap[contentType]
            }
    }

    fun getVocabularyList(
        fileName: String,
    ): Mono<List<String>> {
        return getVocabularyList(segmenterService.getBaseLanguage(fileName).first)
    }

    fun getDocumentFormatterCompiler(
        workflowHandleId: String,
        fileName: String,
    ): Mono<TokenFormatterCompiler<DiffLiteralSourceDocument, DiffOperation>> {
        val repositoryModel = repoModelMap[workflowHandleId]
        val repoTrainedFormatter = repositoryModel?.getDocumentFormatterCompiler(fileName)
        if (repoTrainedFormatter != null) {
            return Mono.just(repoTrainedFormatter)
        }

        return getVocabularyList(fileName)
            .map<TokenFormatterCompiler<DiffLiteralSourceDocument, DiffOperation>> { vocabulary ->
                logger.info("Falling back to document-based indent formatter for $fileName")

                IndentTokenFormatterCompiler(
                    segmenterService,
                    fileName,
                    vocabulary,
                )
            }
            .defaultIfEmpty(
                TokenFormatterCompiler.plain() // TODO: Whitespace unit indenting
            )
    }

    fun fileContentWasUpdated(
        workflowHandleId: String,
        filePath: String,
        oldContent: String?,
        newContent: String?,
    ) {
        val vocabMap = getVocabMap()
            ?: run {
                logger.warn("Ignoring file content update to $filePath because vocab is not yet loaded")
                return
            }

        val repositoryModel = repoModelMap.computeIfAbsent(workflowHandleId) {
            logger.info("Initializing new IndentAlignmentRepositoryModel for workflow $workflowHandleId via $filePath")
            IndentAlignmentRepositoryModel(
                segmenterService,
                vocabMap,
                workflowHandleId,
                trainingScoreAdmissibleThreshold.value,
            )
        }

        repositoryModel.fileContentWasUpdated(filePath, oldContent, newContent)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IndentAlignmentService::class.java)
    }

}