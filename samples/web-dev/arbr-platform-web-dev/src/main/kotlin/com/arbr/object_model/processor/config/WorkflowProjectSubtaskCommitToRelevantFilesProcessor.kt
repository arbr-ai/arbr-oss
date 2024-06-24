package com.arbr.object_model.processor.config

import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialCommitRelevantFile
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrCommit
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileSummaryValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.inference.embedding.FileEmbeddingSearchFunctions
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePaths
import com.arbr.prompt_library.util.FilePathsAndSummaries
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*
import kotlin.math.max

/**
 * Map tasks to relevant files.
 * Ideally this would be a binary operator
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.project-file-based-details", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectSubtaskCommitToRelevantFilesProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val fileEmbeddingSearchFunctions: FileEmbeddingSearchFunctions,
) : ArbrResourceFunction<
        ArbrCommit, PartialCommit,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject, // Really <Commit, File>
        >(objectModelParser) {
    private val fileSearchApplication = promptLibrary.fileSearchApplication

    override val name: String
        get() = "project-file-based-details"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrCommit,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        // Note this is not technically correct - we may want to update the commit's relevant files as more are added,
        // but until a better state update propagation system is implemented it's better to not keep this edge alive
        // TODO: Check post conditions before preparing
        if (listenResource.commitRelevantFiles.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val projectFullName = require(readResource.fullName)

        val projectFiles = require(readResource.files)
        val commitMessage = require(listenResource.commitMessage)
        val commitDiffSummary = require(listenResource.diffSummary)
        val fileOps = require(listenResource.fileOps)
        val fileOpsValue = FileOperationsAndTargetFilePaths.initializeMerged(
            fileOps.map { (_, fileOp) ->
                val file = requireAttached(fileOp.targetFile)

                SourcedStruct2(
                    require(fileOp.fileOperation),
                    require(file.filePath),
                )
            }
        )

        val numFilesUpdated = fileOps.size
        val targetNumFiles = max(maxNumFiles, numFilesUpdated)

        val filesWithSummariesValue: NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileSummaryValue> = FilePathsAndSummaries.initializeMerged(
            projectFiles.mapNotNull { (_, file) ->
                val filePathValue = require(file.filePath)

                if (file.content.getLatestAcceptedValue() != null) {
                    // If the file has content, require a summary
                    val fileSummaryValue = require(file.summary)
                    SourcedStruct2(
                        filePathValue,
                        fileSummaryValue,
                    )
                } else {
                    null
                }
            }
        )

        // Match files by last ID for now - need to support version IDs to resolve multiples
        val filesByPath =
            projectFiles.values.groupBy { file ->
                val filePathValue = require(file.filePath)

                filePathValue.value
            }

        return { partialObjectGraph ->
            val commit = partialObjectGraph.get<PartialCommit>(listenResource.uuid)!!

            fileEmbeddingSearchFunctions
                .embeddingSearchFilePathsAndSummariesValue(
                    volumeState,
                    commitMessage,
                    filesWithSummariesValue,
                    maxNumFileSummariesIncluded,
                )
                .flatMap { filteredFilesWithSummariesValue ->
                    fileSearchApplication.invoke(
                        projectFullName,
                        commitMessage,
                        commitDiffSummary,
                        fileOpsValue,
                        filteredFilesWithSummariesValue,
                        artifactSink.adapt(),
                    ).map { (filePathsValue) ->

                        filePathsValue.containers.mapNotNull { (filePath) ->
                            // Consider a fuzzier match
                            val file = filesByPath[filePath.value]?.lastOrNull()
                            if (file == null) {
                                logger.warn("Suggested task-relevant file ${filePath.value} not found in project state")
                                null
                            } else {
                                PartialCommitRelevantFile(
                                    partialObjectGraph,
                                    UUID.randomUUID().toString(),
                                ).apply {
                                    this.parent = PartialRef(listenResource.uuid)
                                    this.file = PartialRef(file.uuid)
                                }
                            }
                        }.take(targetNumFiles)
                    }
                        .map {
                            commit.commitRelevantFiles = immutableLinkedMapOfPartials(it)
                        }
                        .then()
                }
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrCommit,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(listenResource.commitRelevantFiles.items)

        readResource.files.items.getLatestAcceptedValue()?.forEach { (_, ref) ->
            ref.resource()?.let { file ->
                acquire(file.commitRelevantFiles.items)
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrCommit,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val relevantFiles = requireLatestChildren(listenResource.commitRelevantFiles.items)

        relevantFiles.forEach { (_, fileRelation) ->
            requireLatestAttached(fileRelation.file)
            requireLatestAttached(fileRelation.parent)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectSubtaskCommitToRelevantFilesProcessor::class.java)

        private const val maxNumFileSummariesIncluded = 24
        private const val maxNumFiles = 3
    }
}