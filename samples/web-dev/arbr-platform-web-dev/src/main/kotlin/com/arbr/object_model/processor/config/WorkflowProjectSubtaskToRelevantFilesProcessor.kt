package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.partial.PartialSubtaskRelevantFile
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.inference.embedding.FileEmbeddingSearchFunctions
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FilePathsAndSummaries
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Map subtasks to relevant files.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.subtask-relevant-files", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectSubtaskToRelevantFilesProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val fileEmbeddingSearchFunctions: FileEmbeddingSearchFunctions,
) : ArbrResourceFunction<
        ArbrSubtask, PartialSubtask,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "subtask-relevant-files"
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val fileSearchApplication = promptLibrary.subtaskFileSearchApplication

    override fun prepareUpdate(
        listenResource: ArbrSubtask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        // Note this is not technically correct - we may want to update the subtask's relevant files as more are added,
        // but until a better state update propagation system is implemented it's better to not keep this edge alive
        if (listenResource.subtaskRelevantFiles.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(readResource.fullName)
        val projectFiles = require(readResource.files)
        val subtaskQuery = require(listenResource.subtask)

        val filesWithSummariesValue = FilePathsAndSummaries.initializeMerged(
            projectFiles.map { (_, file) ->
                val filePathValue = require(file.filePath)
                val fileSummaryValue = require(file.summary)

                SourcedStruct2(
                    filePathValue,
                    fileSummaryValue,
                )
            }
        )

        // Match files by last ID for now - need to support version IDs to resolve multiples
        val filesByPath =
            projectFiles.values.groupBy { file ->
                val filePathValue = require(file.filePath)

                filePathValue.value
            }

        return { partialObjectGraph ->
            fileEmbeddingSearchFunctions
                .embeddingSearchFilePathsAndSummariesValue(
                    volumeState,
                    subtaskQuery,
                    filesWithSummariesValue,
                    maxNumFileSummariesIncluded
                )
                .flatMap { filteredFilesWithSummariesValue ->
                    fileSearchApplication.invoke(
                        projectFullName,
                        subtaskQuery,
                        filteredFilesWithSummariesValue,
                        artifactSink.adapt(),
                    ).map { (filePathsValue) ->
                        filePathsValue.containers.mapNotNull { (filePath) ->
                            // Consider a fuzzier match
                            val file = filesByPath[filePath.value]?.lastOrNull()
                            if (file == null) {
                                logger.warn("Suggested subtask-relevant file ${filePath.value} not found in project state")
                                null
                            } else {
                                PartialSubtaskRelevantFile(
                                    partialObjectGraph,
                                    UUID.randomUUID().toString(),
                                ).apply {
                                    parent = PartialRef(listenResource.uuid)
                                    this.file = PartialRef(file.uuid)
                                }
                            }
                        }.take(maxNumFilesOutputted)
                    }
                        .map {
                            partialObjectGraph.get<PartialSubtask>(listenResource.uuid)!!.subtaskRelevantFiles =
                                immutableLinkedMapOfPartials(it)
                        }
                        .then()
                }
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrSubtask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(listenResource.subtaskRelevantFiles.items)

        readResource.files.items.getLatestAcceptedValue()?.forEach { (_, ref) ->
            ref.resource()?.let { file ->
                acquire(file.subtaskRelevantFiles.items)
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrSubtask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val relevantFiles = requireLatestChildren(listenResource.subtaskRelevantFiles.items)

        relevantFiles.forEach { (_, fileRelation) ->
            requireLatestAttached(fileRelation.file)
            requireLatestAttached(fileRelation.parent)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectSubtaskToRelevantFilesProcessor::class.java)

        /**
         * Max number of input files to include in search
         */
        private const val maxNumFileSummariesIncluded = 24

        private const val maxNumFilesOutputted = 5
    }
}