package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.*
import com.arbr.object_model.core.resource.*
import com.arbr.object_model.core.resource.field.*
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.og.object_model.common.model.collections.NestedObjectListType3
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.og.object_model.common.values.collections.SourcedStruct4
import com.arbr.og.object_model.impl.*
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.core.PostConditionFailedException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.*
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import org.apache.commons.text.similarity.LevenshteinDistance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Subtask + Commits + File Ops + Implementations -> Completion eval
 * Commit -> Subtask Uptree - 1
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.subtask-completion", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowSubtaskCompletionProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    @Value("\${topdown.processors.subtask_max_extensions:4}")
    private val maxSubtaskExtensions: Int,
) : ArbrResourceFunction<
        ArbrCommit, PartialCommit,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "subtask-completion"

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    /**
     * Map for deduplicating subtask evaluations - see [WorkflowCommitCompletionProcessor]
     */
    private val subtaskUpdateLock = ConcurrentHashMap<String, Mono<Void>>()

    private val levenshtein = LevenshteinDistance.getDefaultInstance()

    private val commitReassignmentToSubtask = promptLibrary.commitReassignmentToSubtask

    private val subtaskCompletionApplication = promptLibrary.subtaskCompletionApplication

    private fun getSubtaskEval(
        projectFullName: ArbrProjectFullNameValue,
        taskQuery: ArbrTaskTaskQueryValue,
        subtasksValue: SubtasksValue,
        subtaskQuery: ArbrSubtaskSubtaskValue,
        commitDetailsAndFileOps: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>>,
        artifactSink: FluxSink<Artifact>,
    ) = subtaskCompletionApplication.invoke(
        projectFullName,
        taskQuery,
        subtasksValue,
        subtaskQuery,
        commitDetailsAndFileOps,
        artifactSink.adapt(),
    ).flatMap { (partiallyCompleteEval, mostlyCompleteEval, completeEval, cdf) ->
        // Filter to extension commits that actually belong in this subtask
        // GPT-3.5 really really wants to suggest commits from other subtasks, even with explicit instructions and
        // examples to the contrary.
        commitReassignmentToSubtask.invoke(
            projectFullName,
            taskQuery,
            subtasksValue,
            cdf,
            artifactSink.adapt(),
        ).map { (commitMessageSubtaskPairs) ->
            val cdfContainers = cdf.containers
            val subtaskPairsContainers = commitMessageSubtaskPairs.containers

            // Establish mappings of canonical commit messages and subtasks to indices in their respective lists
            val canonicalCommitMessageIndices = cdfContainers.withIndex().associate { (i, v) -> v.t1.value to i }
            val canonicalSubtaskIndices = subtasksValue.containers.withIndex().associate { (i, v) -> v.t1.value to i }
            val thisSubtaskIndex = canonicalSubtaskIndices[subtaskQuery.value]!!

            val acceptedCDFs = subtaskPairsContainers.mapNotNull { (commitMessage, subtask) ->
                val commitMessageIndex = canonicalCommitMessageIndices[commitMessage.value] ?: run {
                    canonicalCommitMessageIndices.entries.minByOrNull { (cm, _) ->
                        levenshtein.apply(commitMessage.value, cm)
                    }?.value
                }

                val subtaskIndex = canonicalSubtaskIndices[subtask.value] ?: run {
                    canonicalSubtaskIndices.entries.minByOrNull { (sq, _) ->
                        levenshtein.apply(subtask.value, sq)
                    }?.value
                }

                if (commitMessageIndex != null && subtaskIndex == thisSubtaskIndex) {
                    cdfContainers[commitMessageIndex]
                } else {
                    null
                }
            }

            SourcedStruct4(
                partiallyCompleteEval,
                mostlyCompleteEval,
                completeEval,
                CommitDetailsAndFileOps.initializeMerged(acceptedCDFs)
            )
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrCommit,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val tasks = require(readResource.tasks).values.toList()
        val subtasks = tasks.flatMap { require(it.subtasks).values }

        val subtaskResource = requireAttached(listenResource.parent)
        val task = requireAttached(subtaskResource.parent)

        val projectFullName = require(readResource.fullName)
        val taskQuery = require(task.taskQuery)

        // Look for subtask containing this commit
        val subtaskQuery = require(subtaskResource.subtask)

        val existingSubtaskEvals =
            subtaskResource.subtaskEvals.items.getLatestValue()?.mapNotNull { it.value.resource() }
        if (existingSubtaskEvals != null) {
            // Do not perform new evals if any exists on the subtask that is marked complete or mostly complete
            if (existingSubtaskEvals.any { it.complete.getLatestValue()?.value == true || it.mostlyComplete.getLatestValue()?.value == true }) {
                throw OperationCompleteException()
            }
        }

        val subtaskPlansContainers = subtasks.map { subtask ->
            val subtaskStatement = require(subtask.subtask)

            SubtasksContainer(
                subtaskStatement,
            )
        }

        val subtasksValue = Subtasks.initializeMerged(subtaskPlansContainers)

        // Commits from this subtask
        val commits = require(subtaskResource.commits)

        val commitDetailContainers = commits.map { (_, commit) ->
            // Require that all commits are committed
            require(commit.commitHash)

            // Require that all commits have evals
            requireThatValue(commit.commitEvals) { completionStatusItems ->
                completionStatusItems.isNotEmpty()
            }

            val commitMessage = require(commit.commitMessage)

            SourcedStruct3(
                commitMessage,
                require(commit.diffSummary),
                FileOperationsAndTargetFilePaths.initializeMerged(
                    require(commit.fileOps)
                        .map { (_, fileOp) ->
                            SourcedStruct2(
                                require(fileOp.fileOperation),
                                require(requireAttached(fileOp.targetFile).filePath),
                            )
                        }
                )
            )
        }

        val commitDetailsAndFileOps: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>> =
            CommitDetailsAndFileOps.initializeMerged(commitDetailContainers)

        val projectFileByPath = require(readResource.files).toList()
            .filter { it.second.filePath.getLatestAcceptedValue() != null }
            .groupBy { it.second.filePath.getLatestAcceptedValue()!!.value }
            .mapValues { (_, files) ->
                files.lastOrNull()?.second
            }

        // Deduplicate operations on the basis of parent + ordered sibling UUIDs
        val commitsLockKey = subtaskResource.uuid + ":" + commits.values.joinToString(":") { it.uuid }

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root

            subtaskUpdateLock.computeIfAbsent(commitsLockKey) {
                val updateMono = getSubtaskEval(
                    projectFullName,
                    taskQuery,
                    subtasksValue,
                    subtaskQuery,
                    commitDetailsAndFileOps,
                    artifactSink,
                ).map { (partiallyCompleteEval, mostlyCompleteEval, completeEval, cdf) ->
                    val partiallyComplete: ArbrSubtaskEvalPartiallyCompleteValue
                    val mostlyComplete: ArbrSubtaskEvalMostlyCompleteValue
                    val complete: ArbrSubtaskEvalCompleteValue
                    if (existingSubtaskEvals != null && existingSubtaskEvals.size >= maxSubtaskExtensions) {
                        // Reached max subtask extensions attempts
                        // TODO: Implement rejection
                        logger.warn("Reached max subtask extensions, accepting: ${subtaskQuery.value}")
                        partiallyComplete =
                            ArbrSubtaskEval.PartiallyComplete.computed(
                                true,
                                partiallyCompleteEval.generatorInfo,
                            )
                        mostlyComplete = ArbrSubtaskEval.MostlyComplete.computed(
                            true,
                            mostlyCompleteEval.generatorInfo,
                        )
                        complete = completeEval
                    } else {
                        partiallyComplete = partiallyCompleteEval
                        mostlyComplete = mostlyCompleteEval
                        complete = completeEval
                    }

                    if (!complete.value && !mostlyComplete.value && !partiallyComplete.value) {
                        // TODO: Consider backtracking
                        logger.warn("No progress made on subtask according to eval")
                    }

                    val newFiles = mutableListOf<PartialFile>()

                    val newCommits = if (complete.value || mostlyComplete.value) {
                        // Mostly done - don't push any more operations
                        logger.info("Subtask eval judged complete! Ready to move on from ${subtaskQuery.value}")
                        emptyList()
                    } else {
                        cdf.containers.mapNotNull { (commitMessage, diffSummary, fileOps) ->
                            val newCommit = PartialCommit(
                                partialObjectGraph,
                                UUID.randomUUID().toString(),
                            ).apply {
                                this.commitMessage = commitMessage
                                this.diffSummary = diffSummary
                                parent = PartialRef(subtaskResource.uuid)
                            }

                            val newFileOps = fileOps.containers.mapNotNull { (fileOp, filePath) ->
                                val file = projectFileByPath[filePath.value]
                                if (file == null) {
                                    // Create a new file with no content
                                    val newFile = PartialFile(
                                        partialObjectGraph,
                                        UUID.randomUUID().toString(),
                                    ).apply {
                                        parent = PartialRef(writeProject.uuid)
                                        this.filePath = filePath
                                        content = ArbrFile.Content.generated(
                                            null,
                                            filePath.generatorInfo
                                        ) // This file is known to be new, so initialize it with null contents
                                    }
                                    newFiles.add(newFile)

                                    FileOpPartialHelper.fileOpPartial(
                                        partialObjectGraph,
                                        commit = newCommit,
                                        file = newFile,
                                        fileOperation = fileOp,
                                        description = null, // These get added by the detail processor
                                        commitEval = PartialRef(null), // TODO: Probably want this set
                                    )
                                } else {
                                    FileOpPartialHelper.fileOpPartial(
                                        partialObjectGraph,
                                        commit = newCommit,
                                        file = file,
                                        fileOperation = fileOp,
                                        description = null, // Could be useful
                                        commitEval = PartialRef(null), // TODO: Probably want this set
                                    )
                                }
                            }

                            if (newFileOps.isEmpty()) {
                                logger.info("Subtask eval resulted in commit with no file ops, skipping")
                                null
                            } else {
                                newCommit.apply {
                                    this.fileOps = immutableLinkedMapOfPartials(newFileOps)
                                }
                            }
                        }
                    }

                    if (newFiles.isNotEmpty()) {
                        logger.info("Subtask eval creating new files: ${newFiles.joinToString(", ") { it.filePath?.value ?: "" }}")
                    }

                    val currentCommits = commits.values.mapNotNull {
                        partialObjectGraph.get<PartialCommit>(it.uuid)
                    }
                    val currentCommitMessages = currentCommits.mapNotNull {
                        it.commitMessage?.value?.trim()?.lowercase()
                    }

                    // Fuzzy match commit messages
                    val filteredNewCommits = newCommits.filter {
                        val commitMessageNorm = it.commitMessage?.value?.trim()?.lowercase()
                        commitMessageNorm != null && commitMessageNorm !in currentCommitMessages
                    }
                    val nextCommits = currentCommits + filteredNewCommits
                    logger.info(
                        "Extending subtask with ${filteredNewCommits.size} commits:\n" + filteredNewCommits.joinToString(
                            "\n"
                        ) {
                            it.commitMessage?.value ?: "blank"
                        })

                    val nextCommitsFiltered = ImmutableLinkedMap(
                        *nextCommits
                            .map { it.uuid to it }
                            .toTypedArray()
                    )

                    // If no additional commits are suggested, overwrite the completion status
                    val newStatus = PartialSubtaskEval(
                        partialObjectGraph,
                        UUID.randomUUID().toString(),
                    ).apply {
                        this.complete = complete
                        this.mostlyComplete = if (filteredNewCommits.isEmpty()) {
                            logger.info("No new commits in subtask eval, marking mostly complete")
                            ArbrSubtaskEval.MostlyComplete.computed(
                                true,
                                mostlyCompleteEval.generatorInfo,
                            )
                        } else {
                            mostlyComplete
                        }
                        this.partiallyComplete =
                            ArbrSubtaskEval.PartiallyComplete.computed(
                                true,
                                partiallyCompleteEval.generatorInfo,
                            )
                        parent = PartialRef(subtaskResource.uuid)
                    }

                    val subtask = partialObjectGraph.get<PartialSubtask>(subtaskResource.uuid)!!
                    subtask.commits = nextCommitsFiltered
                    subtask.subtaskEvals =
                        (subtask.subtaskEvals ?: ImmutableLinkedMap()).adding(
                            newStatus.uuid,
                            newStatus,
                        )

                    writeProject.files = (writeProject.files
                        ?: ImmutableLinkedMap()).updatingFromPairs(newFiles.map { it.uuid to it })
                }

                Mono.defer {
                    logger.info("Running subtask eval on ${subtaskResource.uuid}")
                    updateMono.then()
                }.cache(
                    /* ttlForValue = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
                    /* ttlForError = */ { Duration.ZERO }, // Do not cache errors since we expect retries
                    /* ttlForEmpty = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
                )
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
        val subtask = requireLatestAttached(listenResource.parent)
        // Require some completion statuses in existence
        val completionStatuses = requireLatestChildren(subtask.subtaskEvals.items).values
        val lastCompletionStatus = completionStatuses.lastOrNull()
            ?: throw PostConditionFailedException("Subtask had no completion statuses after eval")

        // Require evals present on the last status
        val isComplete = requireLatest(lastCompletionStatus.complete).value
        val isMostlyComplete = requireLatest(lastCompletionStatus.mostlyComplete).value
        requireLatest(lastCompletionStatus.partiallyComplete)

        // Either acceptable or has some commits
        if (!isComplete && !isMostlyComplete) {
            val subtasks = requireLatestChildren(lastCompletionStatus.commits.items)
            if (subtasks.isEmpty()) {
                throw PostConditionFailedException("No new commits but subtask eval is unacceptable")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowSubtaskCompletionProcessor::class.java)
    }
}