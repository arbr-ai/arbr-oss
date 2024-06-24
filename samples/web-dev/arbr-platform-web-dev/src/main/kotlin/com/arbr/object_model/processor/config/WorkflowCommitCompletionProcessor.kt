package com.arbr.object_model.processor.config

import com.arbr.content_formats.tokens.TokenizationUtils
import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.object_model.core.partial.*
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileOpDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrFileOpFileOperationValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.inference.embedding.FileEmbeddingSearchFunctions
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.PostConditionFailedException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FilePathsAndContents
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.Duration
import java.util.*

/**
 * Commit + File Ops + Implementations -> Completion eval
 *
 * Uptree -1
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.commit-completion", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowCommitCompletionProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    @Value("\${topdown.processors.commit_max_extensions:4}")
    private val maxCommitExtensions: Int,
    @Value("\${topdown.processors.commit_ignore_extensions:false}")
    private val ignoreCommitExtensions: Boolean,
    private val fileEmbeddingSearchFunctions: FileEmbeddingSearchFunctions,
) : ArbrResourceFunction<
        ArbrFileOp, PartialFileOp,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    private val commitCompletionEvaluator = promptLibrary.contextualCommitCompletionApplication

    override val name: String
        get() = "commit-completion"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrFileOp,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val projectFullName = require(readResource.fullName)

        val commit = requireAttachedOrElseComplete(listenResource.parent)
        val subtaskResource = requireAttachedOrElseComplete(commit.parent)
        val task = requireAttachedOrElseComplete(subtaskResource.parent)

        val taskQuery = require(task.taskQuery)
        val projectFiles = require(readResource.files)

        val fileOps = require(commit.fileOps).values

        val existingCommitEvals = commit.commitEvals.items.getLatestAcceptedValue()

        val commitMessage = require(commit.commitMessage)
        val diffSummary = require(commit.diffSummary)

        val subtask = require(subtaskResource.subtask)

        val commitRelevantFiles = require(commit.commitRelevantFiles)

        val updatedFileContainers = fileOps
            .map { fileOp ->
                // Require a proposed implementation of each file op, as a reference to the implemented file version
                val implementedFile = requireAttached(fileOp.implementedFile)
                val filePathValue = require(implementedFile.filePath)
                val fileContentValue = require(implementedFile.content)

                SourcedStruct2(
                    filePathValue,
                    fileContentValue,
                )
            }
        val updatedFilesWithContentsValue = FilePathsAndContents.initializeMerged(
            updatedFileContainers
        )

        val relevantFilesWithContentsValue: NestedObjectListType2.Value<String, String?, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue, ArbrFileContentValue> = FilePathsAndContents.initializeMerged(
            commitRelevantFiles
                .mapNotNull { (_, fileRelation) ->
                    fileRelation.file.getLatestAcceptedValue()?.resource()?.let { projectFiles[it.uuid] }
                }
                .map { file ->
                    val filePathValue = require(file.filePath)
                    val fileContentValue = require(file.content)

                    SourcedStruct2(
                        filePathValue,
                        fileContentValue,
                    )
                }
                .filter { (filePath, _) ->
                    !updatedFileContainers.any {
                        it.t1.value == filePath.value
                    }
                }
        )

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root
            val writeCommit = partialObjectGraph.get<PartialCommit>(commit.uuid)!!
            val writeFileOps = writeCommit.fileOps!!.values

            // Create the status object and mutate it later so that the new file ops can reference it
            val newStatus = PartialCommitEval(
                partialObjectGraph,
                UUID.randomUUID().toString(),
            ).apply {
                this.parent = PartialRef(writeCommit.uuid)
            }

            logger.info("Checking commit completion for ${commit.uuid} via ${listenResource.uuid}")

            // This processor has had issues with large inputs - remove this when fixed
            logger.info(
                "Relevant files n=${relevantFilesWithContentsValue.containers.size} token_count=${
                    relevantFilesWithContentsValue.value.sumOf {
                        it.t2?.let { c -> TokenizationUtils.getTokenCount(c) } ?: 0
                    }
                }"
            )
            logger.info(
                "Updated files n=${updatedFilesWithContentsValue.containers.size} token_count=${
                    updatedFilesWithContentsValue.value.sumOf {
                        it.t2?.let { c -> TokenizationUtils.getTokenCount(c) } ?: 0
                    }
                }"
            )
            val maxNumResults = 8 // Use a generous file count limit, since token limit matters more
            val updateMono = Mono.zip(
                fileEmbeddingSearchFunctions.embeddingSearchFilePathsAndContentsValue(
                    volumeState,
                    taskQuery,
                    relevantFilesWithContentsValue,
                    maxNumResults,
                )
                    .onErrorReturn(relevantFilesWithContentsValue)
                    .defaultIfEmpty(relevantFilesWithContentsValue),
                fileEmbeddingSearchFunctions.embeddingSearchFilePathsAndContentsValue(
                    volumeState,
                    taskQuery,
                    updatedFilesWithContentsValue,
                    maxNumResults,
                )
                    .onErrorReturn(updatedFilesWithContentsValue)
                    .defaultIfEmpty(updatedFilesWithContentsValue),
            ).flatMap { (filteredRelevantFilesWithContentsValue, filteredUpdatedFilesWithContentsValue) ->
                commitCompletionEvaluator.invoke(
                    projectFullName,
                    taskQuery,
                    subtask,
                    commitMessage,
                    diffSummary,
                    filteredRelevantFilesWithContentsValue,
                    filteredUpdatedFilesWithContentsValue,
                    artifactSink.adapt()
                )
            }.map { (partiallyCompleteEval, mostlyCompleteEval, completeEval, fileOpsWithDescriptions) ->
                // Compute file ops to APPEND to list
                val brandNewCommitFileOps =
                    if (ignoreCommitExtensions) {
                        logger.warn("Ignoring extensions and accepting commit: ${commitMessage.value}")

                        ImmutableLinkedMap()
                    } else if (existingCommitEvals != null && existingCommitEvals.size >= maxCommitExtensions) {
                        // Reached max commit attempts
                        // TODO: Implement rejection
                        logger.warn("Reached max commit attempts, accepting: ${commitMessage.value}")

                        // No new file ops
                        ImmutableLinkedMap()
                    } else if (completeEval.value || mostlyCompleteEval.value) {
                        // Mostly done - don't push any more operations
                        logger.info("Commit eval judged complete! Ready to commit ${commitMessage.value}")

                        // No new file ops
                        ImmutableLinkedMap()
                    } else {
                        val projectFileByPath = projectFiles
                            .values
                            .filter { it.filePath.getLatestAcceptedValue() != null }
                            .groupBy { it.filePath.getLatestAcceptedValue()!!.value }
                            .mapValues { (_, files) ->
                                files.lastOrNull()
                            }

                        // Update the file ops associated with the commit to trigger a continuation of the commit:
                        //  - USED TO, NOT ANYMORE: For file ops that match an op already in the commit, just detach the implemented file
                        //  - For brand new file ops, add them on
//                        val existingFilePaths = writeFileOps.mapNotNull { fileOp ->
//                            fileOp.targetFile.let { tf ->
//                                projectFiles[tf?.uuid]?.filePath?.getLatestAcceptedValue()?.value
//                            }
//                        }
                        val newFileOpRecords = mutableMapOf<String, SourcedStruct3<
                                ArbrFileOpFileOperationValue,
                                ArbrFileFilePathValue,
                                ArbrFileOpDescriptionValue,
                                >>()

                        val brandNewFileOps =
                            fileOpsWithDescriptions.containers
                                .mapNotNull { (fileOp, filePath, description) ->
                                    newFileOpRecords[filePath.value] =
                                        SourcedStruct3(fileOp, filePath, description)

                                    val file = projectFileByPath[filePath.value]
                                    if (file == null) {
                                        // Create a new file with no content
                                        val newFile = PartialFile(
                                            partialObjectGraph,
                                            UUID.randomUUID().toString(),
                                        ).apply {
                                            this.parent = PartialRef(writeProject.uuid)
                                            this.filePath = filePath
                                            content = ArbrFile.Content.generated(
                                                null,
                                                filePath.generatorInfo
                                            ) // This file is known to be new, so initialize it with null contents
                                        }

                                        FileOpPartialHelper.fileOpPartial(
                                            partialObjectGraph,
                                            commit = writeCommit,
                                            file = newFile,
                                            fileOperation = fileOp,
                                            description = description,
                                            commitEval = PartialRef(newStatus.uuid),
                                        )
                                    } else {
                                        FileOpPartialHelper.fileOpPartial(
                                            partialObjectGraph,
                                            commit = writeCommit,
                                            file = file,
                                            fileOperation = fileOp,
                                            description = description,
                                            commitEval = PartialRef(newStatus.uuid),
                                        )
                                    }
                                }

                        val completedCount = writeFileOps.size
                        val incompleteCount = brandNewFileOps.size

                        logger.info("Commit eval brought from ${fileOps.size} file ops completed to $completedCount completed and $incompleteCount incomplete")

                        // Potentially some new file ops
                        ImmutableLinkedMap(
                            *brandNewFileOps.map { it.uuid to it }
                                .toTypedArray()
                        )
                    }

                val anyNewFileOps = brandNewCommitFileOps.any {
                    it.value.implementedFile == null
                }

                // Potentially overwrite values of partiallyComplete + mostlyComplete if there are no
                // new file ops
                val partiallyComplete =
                    ArbrCommitEval.PartiallyComplete.computed(
                        !anyNewFileOps,
                        partiallyCompleteEval.generatorInfo,
                    )
                val mostlyComplete =
                    ArbrCommitEval.MostlyComplete.computed(
                        !anyNewFileOps,
                        mostlyCompleteEval.generatorInfo,
                    )

                newStatus.apply {
                    complete = completeEval
                    this.mostlyComplete = mostlyComplete
                    this.partiallyComplete = partiallyComplete
                }

                brandNewCommitFileOps.values.forEach {
                    it.commitEval = PartialRef(newStatus.uuid)
                }

                writeCommit.fileOps =
                    (writeCommit.fileOps ?: ImmutableLinkedMap()).updatingFrom(
                        brandNewCommitFileOps
                    )

                logger.info("Adding completion status to ${commit.uuid} via ${listenResource.uuid}")
                logger.info("New status: ${newStatus.uuid} complete=${newStatus.complete?.value} mostlyComplete=${newStatus.mostlyComplete?.value}")
                writeCommit.commitEvals =
                    (writeCommit.commitEvals ?: ImmutableLinkedMap()).adding(
                        newStatus.uuid, newStatus
                    )
            }

            Mono.defer {
                logger.info("Running commit eval on ${commit.uuid} via child ${listenResource.uuid}")
                updateMono.then()
            }.cache(
                /* ttlForValue = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
                /* ttlForError = */ { Duration.ZERO }, // Do not cache errors since we expect retries
                /* ttlForEmpty = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
            )
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileOp,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        val commit = listenResource.parent.getLatestValue()?.resource()
            ?: return
        val subtaskResource = commit.parent.getLatestValue()?.resource()
            ?: return
        subtaskResource.parent.getLatestValue()?.resource()
            ?: return

        // Require some completion statuses in existence
        val completionStatuses = requireLatestChildren(commit.commitEvals.items)
        val lastCompletionStatus = completionStatuses.values.lastOrNull()
            ?: throw PostConditionFailedException("Commit had no completion statuses after eval")

        // Require evals present on the last status
        val isComplete = requireLatest(lastCompletionStatus.complete).value
        val isMostlyComplete = requireLatest(lastCompletionStatus.mostlyComplete).value
        requireLatest(lastCompletionStatus.partiallyComplete)

        // Either acceptable or created new file ops
        if (!isComplete && !isMostlyComplete) {
            val fileOps = requireLatestChildren(commit.fileOps.items)
            val someUnimplemented = fileOps.any { (_, fileOp) ->
                fileOp.implementedFile.getLatestValue() == null
            }

            if (!someUnimplemented) {
                throw PostConditionFailedException("No unimplemented file ops after commit eval")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowCommitCompletionProcessor::class.java)
    }
}