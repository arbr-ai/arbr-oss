package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrCommit
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.core.PostConditionFailedException
import com.arbr.og_engine.core.RequiredLockedResourceRenderException
import com.arbr.og_engine.core.RequiredResourceMissingException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.typing.cls
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Take file segments with incomplete implementations as input and output plans for implementation
 *
 * Note: this is really a binary operator on (Task, FileSegment) which potentially adds a subtask to the task
 * Should switch to binary when they're implemented
 *
 * TODO: Limit to files changed by this task (or relevant)
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.todo-impl-plan", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowTodoProcessor(
    objectModelParser: ObjectModelParser,
) : ArbrResourceFunction<
        ArbrFileSegment, PartialFileSegment,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    override val name: String
        get() = "todo-impl-plan"

    override val targetResourceClass: Class<ArbrProject> = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    /**
     * Common parent for TODOs
     */
    private val todoSubtaskStatement = ArbrSubtask.Subtask.constant(
        "Address remaining TODOs"
    )

    private val todoFileOp = ArbrFileOp.FileOperation.constant("edit_file")

    /**
     * Plan and propose a single subtask as a container, i.e. a subtask statement + list of commit details and file ops
     */
    private fun planAndProposeSingleSubtask(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        project: PartialProject,
        task: PartialTask,
        file: PartialFile,
        filePath: ArbrFileFilePathValue,
    ) {
        val projectFileByUUID = project.files ?: ImmutableLinkedMap()

        val todoCommitMessage =
            ArbrCommit.CommitMessage.constant("Address TODOs in ${filePath.value}")
        val todoDiffSummary =
            ArbrCommit.DiffSummary.constant("Address TODOs in ${filePath.value}")

        val subtasks = task.subtasks ?: ImmutableLinkedMap()

        // Merge with any existing subtask
        // Cases like this are why an in-memory DB would work well
        var subtaskIsNew = false
        val subtask = subtasks.entries.firstOrNull { (_, subtask) ->
            subtask.subtask?.value == todoSubtaskStatement.value
        }?.value ?: kotlin.run {
            subtaskIsNew = true

            val partialRef =
                PartialRef<ArbrTask, PartialTask>(task.uuid)
            PartialSubtask(
                partialObjectGraph,
                UUID.randomUUID().toString(),
            ).apply {
                this.parent = partialRef
            }
        }

        val commits = subtask.commits ?: ImmutableLinkedMap()
        var commitIsNew = false
        val commit = commits.values.firstOrNull { commit ->
            // Match on message
            commit.commitMessage?.value == todoCommitMessage.value
        } ?: kotlin.run {
            commitIsNew = true

            PartialCommit(
                partialObjectGraph,
                UUID.randomUUID().toString()
            ).apply {
                commitMessage = todoCommitMessage
                diffSummary = todoDiffSummary
                this.parent = PartialRef(subtask.uuid)
            }
        }

        val fileOps = commit.fileOps ?: ImmutableLinkedMap()

        var fileOpIsNew = false
        val fileOp = fileOps.values.firstOrNull { fileOp ->
            fileOp.fileOperation?.value == todoFileOp.value && fileOp.targetFile?.uuid?.let {
                projectFileByUUID[it]
            }?.filePath?.value == file.filePath?.value
        } ?: run {
            fileOpIsNew = true

            FileOpPartialHelper.fileOpPartial(
                partialObjectGraph,
                commit = commit,
                file = file,
                fileOperation = todoFileOp,
                description = null,
                commitEval = PartialRef(null),
            )!! // We know this file exists
        }


        // TODO: Clean up
        if (fileOpIsNew) {
            commit.fileOps = fileOps.adding(fileOp.uuid, fileOp)
        }

        if (commitIsNew) {
            subtask.commits = commits.adding(commit.uuid, commit)
        }

        if (subtaskIsNew) {
            task.subtasks = subtasks.adding(subtask.uuid, subtask)
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrFileSegment,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val containsTodo = require(listenResource.containsTodo)

        // Small hack: assume first project task is the right one so we don't have to dig up from the fileSegment
        val task = require(readResource.tasks).values.firstOrNull()!!

        // If the segment decidedly does not have a TODO, then be done
        if (!containsTodo.value!!) {
            throw OperationCompleteException()
        }

        // Make sure the project is already planned, i.e. has some subtasks
        // Note: currently not passing this since project is planned linearly
        val parentFile = requireAttachedOrElseComplete(listenResource.parent)

        val filePath = require(parentFile.filePath)

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root
            val writeProjectTask = partialObjectGraph.get<PartialTask>(task.uuid)!!
            val writeParentFile = partialObjectGraph.get<PartialFile>(parentFile.uuid)!!

            planAndProposeSingleSubtask(
                partialObjectGraph,
                writeProject,
                writeProjectTask,
                writeParentFile,
                filePath
            )

            Mono.empty()
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrFileSegment,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        val tasks = writeResource.tasks.items.getLatestValue()?.values?.mapNotNull { it.resource() } ?: emptyList()

        val allSubtasks = tasks.flatMap {
            acquire(it.subtasks.items)

            it.subtasks.items.getLatestValue()?.values?.mapNotNull { r -> r.resource() } ?: emptyList()
        }

        val matchingSubtask = allSubtasks.firstOrNull { subtask ->
            subtask.subtask.getLatestValue()?.value == todoSubtaskStatement.value
        }

        fun acquireWriteTargetsRec(
            objectModelResource: ObjectModelResource<*, *, *>,
        ) {
            objectModelResource.properties().values.forEach(acquire)

            objectModelResource.getChildren().values.forEach { fkStream ->
                if (fkStream.hasAnyUnresolvedProposals()) {
                    throw RequiredLockedResourceRenderException(fkStream.identifier.resourceUuid, fkStream.identifier.resourceKey.name)
                }
                fkStream.getLatestAcceptedValue()?.values?.forEach { ref ->
                    ref.resource()?.let { childResource ->
                        acquireWriteTargetsRec(childResource)
                    }
                }
            }
        }

        if (matchingSubtask != null) {
            acquireWriteTargetsRec(matchingSubtask)
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileSegment,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        val task = requireLatestChildren(readTargetResource.tasks.items)
            .values.firstOrNull()
            ?: throw RequiredResourceMissingException(
                readTargetResource.tasks.items,
                readTargetResource.tasks.items::class.java
            )

        if (requireLatest(listenResource.containsTodo).value == true) {
            // Require some commit on some subtask matching the description
            val subtasks = requireLatestChildren(task.subtasks.items)
            val filePath = requireLatest(requireLatestAttached(listenResource.parent).filePath).value
            val somePlanMatches = subtasks.any { (_, subtaskResource) ->
                requireLatest(subtaskResource.subtask).value == todoSubtaskStatement.value
                        && requireLatestChildren(subtaskResource.commits.items).any { (_, commitResource) ->
                    requireLatest(commitResource.commitMessage).value == "Address TODOs in $filePath"
                            && requireLatestChildren(commitResource.fileOps.items).any { (_, fileOpResource) ->
                        requireLatest(fileOpResource.fileOperation).value == todoFileOp.value
                                && requireLatest(requireLatestAttached(fileOpResource.targetFile).filePath).value == filePath
                    }
                }
            }

            if (!somePlanMatches) {
                throw PostConditionFailedException("No plan for TODO implementation after processing")
            }
        }
    }

}
