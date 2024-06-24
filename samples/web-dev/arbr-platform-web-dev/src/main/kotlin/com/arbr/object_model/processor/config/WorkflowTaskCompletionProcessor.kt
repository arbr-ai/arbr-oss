package com.arbr.object_model.processor.config

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.partial.PartialTaskEval
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.ArbrTaskEval
import com.arbr.object_model.core.resource.field.ArbrTaskEvalCompleteValue
import com.arbr.object_model.core.resource.field.ArbrTaskEvalMostlyCompleteValue
import com.arbr.object_model.core.resource.field.ArbrTaskEvalPartiallyCompleteValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.core.PostConditionFailedException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.CommitDetailsAndFileOps
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePaths
import com.arbr.prompt_library.util.SubtaskPlans
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util.adapt
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Subtask + Commits + File Ops + Implementations -> Completion eval
 *
 * 1. Task eval require subtask evals x
 * 2. Commit->Subtask eval Uptree x
 * 3. Subtask eval extension limit x
 * 3. Subtask->Task eval Uptree x
 * 4. Task eval extension limit x
 * 4b. Test
 * 5. Pull request opening
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-completion", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowTaskCompletionProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    @Value("\${topdown.processors.task_max_extensions:2}")
    private val maxTaskExtensions: Int,
) : ArbrResourceFunction<
        ArbrSubtask, PartialSubtask,
        ArbrProject, PartialProject,
        ArbrTask, PartialTask,
        >(objectModelParser) {
    override val name: String
        get() = "task-completion"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrTask>
        get() = cls()

    private val taskCompletionApplication = promptLibrary.taskCompletionApplication

    override fun prepareUpdate(
        listenResource: ArbrSubtask,
        readResource: ArbrProject,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        val projectFullName = require(readResource.fullName)
        val taskQuery = require(writeResource.taskQuery)

        val existingTaskEvals = writeResource.taskEvals.items.getLatestValue()?.values
        if (existingTaskEvals != null) {
            // Do not perform new evals if any exists on the task that is marked complete or mostly complete
            if (existingTaskEvals.any {
                val resource = it.resource()
                resource?.complete?.getLatestValue()?.value == true || resource?.mostlyComplete?.getLatestAcceptedValue()?.value == true
            }) {
                throw OperationCompleteException()
            }
        }

        val subtasks = require(writeResource.subtasks)

        val subtaskPlansContainers = subtasks.map { (_, subtask) ->
            val subtaskStatement = require(subtask.subtask)

            requireThatValue(subtask.subtaskEvals) { subtaskEvals ->
                subtaskEvals.any { subtaskEvalRef ->
                    val subtaskEval = subtaskEvalRef.value
                    val isComplete = require(subtaskEval.complete).value
                    val isMostlyComplete = require(subtaskEval.mostlyComplete).value

                    isComplete || isMostlyComplete
                }
            }

            val commits = require(subtask.commits)
            val commitDetailContainers = commits.map { (_, commit) ->
                // Checks on the completion of commits are implied by the completion of the subtasks
                SourcedStruct3(
                    require(commit.commitMessage),
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

            val commitDetailsAndFileOps = CommitDetailsAndFileOps.initializeMerged(commitDetailContainers)

            SourcedStruct2(
                subtaskStatement,
                commitDetailsAndFileOps,
            )
        }

        val subtaskPlans = SubtaskPlans.initializeMerged(subtaskPlansContainers)

        return { partialObjectGraph ->
            val writeTask = partialObjectGraph.root

            taskCompletionApplication.invoke(
                projectFullName,
                taskQuery,
                subtaskPlans,
                artifactSink.adapt(),
            )
                .map { (partiallyCompleteEval, mostlyCompleteEval, completeEval, remainingSubtasks) ->
                    val partiallyComplete: ArbrTaskEvalPartiallyCompleteValue
                    val mostlyComplete: ArbrTaskEvalMostlyCompleteValue
                    val complete: ArbrTaskEvalCompleteValue
                    if (existingTaskEvals != null && existingTaskEvals.size >= maxTaskExtensions) {
                        // Reached max subtask extensions attempts
                        // TODO: Implement rejection
                        logger.warn("Reached max task extension, accepting: ${taskQuery.value}")
                        partiallyComplete = ArbrTaskEval.PartiallyComplete.computed(
                            true,
                            partiallyCompleteEval.generatorInfo,
                        )
                        mostlyComplete = ArbrTaskEval.MostlyComplete.computed(
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
                        logger.warn("No progress made on task according to eval")
                    }

                    val newSubtasks = if (complete.value || mostlyComplete.value) {
                        // Mostly done - don't push any more operations
                        emptyList()
                    } else {
                        remainingSubtasks.containers.map { (subtaskStatement) ->
                            PartialSubtask(
                                partialObjectGraph,
                                UUID.randomUUID().toString(),
                            ).apply {
                                subtask = subtaskStatement
                                parent = PartialRef(writeResource.uuid)
                            }
                        }
                    }

                    val newStatus = PartialTaskEval(
                        partialObjectGraph,
                        UUID.randomUUID().toString(),
                    ).apply {
                        this.complete = complete
                        this.mostlyComplete = mostlyComplete
                        this.partiallyComplete = partiallyComplete
                        parent = PartialRef(writeResource.uuid)
                    }
                    logger.info("Eval task: $newStatus")

                    logger.info("Extending task with ${newSubtasks.size} subtasks:\n" + newSubtasks.joinToString("\n") {
                        it.subtask?.value ?: "blank"
                    })

                    writeTask.subtasks = (writeTask.subtasks ?: ImmutableLinkedMap()).updatingFrom(
                        immutableLinkedMapOfPartials(newSubtasks)
                    )
                    writeTask.taskEvals = (writeTask.taskEvals ?: ImmutableLinkedMap()).adding(
                        newStatus.uuid, newStatus,
                    )
                }.then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrSubtask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        // Require some completion statuses in existence
        val completionStatuses = requireLatestChildren(writeTargetResource.taskEvals.items).values
        val lastCompletionStatus = completionStatuses.lastOrNull()
            ?: throw PostConditionFailedException("Task had no completion statuses after eval")

        // Require evals present on the last status
        val isComplete = requireLatest(lastCompletionStatus.complete).value
        val isMostlyComplete = requireLatest(lastCompletionStatus.mostlyComplete).value
        requireLatest(lastCompletionStatus.partiallyComplete)

        // Either acceptable or has some subtasks
        if (!isComplete && !isMostlyComplete) {
            val subtasks = requireLatestChildren(lastCompletionStatus.subtasks.items)
            if (subtasks.isEmpty()) {
                throw PostConditionFailedException("No new subtasks but task eval is unacceptable")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowTaskCompletionProcessor::class.java)
    }
}