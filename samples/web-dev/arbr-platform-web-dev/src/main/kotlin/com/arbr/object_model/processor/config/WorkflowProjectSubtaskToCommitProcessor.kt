package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.partial.PartialSubtask
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.resource.field.ArbrCommitCommitMessageValue
import com.arbr.object_model.core.resource.field.ArbrCommitDiffSummaryValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileOpFileOperationValue
import com.arbr.object_model.core.resource.field.ArbrProjectDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectFullNameValue
import com.arbr.object_model.core.resource.field.ArbrProjectPlatformValue
import com.arbr.object_model.core.resource.field.ArbrSubtaskSubtaskValue
import com.arbr.object_model.core.resource.field.ArbrTaskTaskVerbosePlanValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.og.object_model.common.model.collections.NestedObjectListType3
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
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
import com.arbr.prompt_library.util.FilePathsAndSummaries
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Listens for the creation of a project task + verbose plan, then generates subtasks.
 * This is part 2b of planning:
 * 1. Verbose plan -> publish
 * 2a. Breakdown & synthesize -> Subtasks
 * 2b. Subtasks to SubtaskPlans (subtasks + commits)
 * 3. Simplify -> TaskPlan
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.subtask-to-commit", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowProjectSubtaskToCommitProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrSubtask, PartialSubtask,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject,
        >(objectModelParser) {
    private val taskPlanCommitDescriptionsPipelineYaml = promptLibrary.taskPlanCommitDescriptionsPipelineYaml
    private val taskBreakdownSynthesizeCommitsApplication = promptLibrary.taskBreakdownSynthesizeCommitsApplication
    private val taskBreakdownReduceCommitsApplication = promptLibrary.taskBreakdownReduceCommitsApplication
    private val taskDeduplicateCommits = promptLibrary.taskDeduplicateCommits

    override val name: String
        get() = "subtask-to-commit"

    override val targetResourceClass: Class<ArbrProject> = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private fun synthesize(
        projectFullName: ArbrProjectFullNameValue,
        platform: ArbrProjectPlatformValue,
        projectDescription: ArbrProjectDescriptionValue,
        subtaskQuery: ArbrSubtaskSubtaskValue,
        verbosePlan: ArbrTaskTaskVerbosePlanValue,
        commitDetailsAndFileOps: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>>,
        artifactSink: FluxSink<Artifact>,
    ): Mono<NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>>> {
        val numCommits = commitDetailsAndFileOps.value.size
        return if (numCommits > commitSynthesisLimit) {
            val n = numCommits / 2

            val head = commitDetailsAndFileOps.copy(
                value = commitDetailsAndFileOps.value.take(n)
            )
            val tail = commitDetailsAndFileOps.copy(
                value = commitDetailsAndFileOps.value.drop(n)
            )

            Mono.zip(
                synthesize(
                    projectFullName,
                    platform,
                    projectDescription,
                    subtaskQuery,
                    verbosePlan,
                    head,
                    artifactSink,
                ),
                synthesize(
                    projectFullName,
                    platform,
                    projectDescription,
                    subtaskQuery,
                    verbosePlan,
                    tail,
                    artifactSink,
                )
            ).map {
                CommitDetailsAndFileOps.initializeMerged(
                    it.t1.containers + it.t2.containers
                )
            }
        } else if (numCommits <= 1) {
            Mono.just(commitDetailsAndFileOps)
        } else {
            taskBreakdownSynthesizeCommitsApplication.invoke(
                projectFullName,
                platform,
                projectDescription,
                verbosePlan,
                subtaskQuery,
                commitDetailsAndFileOps,
                artifactSink.adapt(),
            ).map { (cd) -> cd }
        }
    }

//    private fun deduplicateCommitInfos(
//        parentTaskInfo: IterationTaskContainer,
//        prefix: CommitDetailsAndFileOpsValue,
//        newSuffix: CommitDetailsAndFileOpsValue,
//        artifactSink: FluxSink<Artifact>,
//    ): Mono<CommitDetailsAndFileOpsValue> = if (prefix.value.isEmpty()) {
//        Mono.just(newSuffix)
//    } else {
//        taskDeduplicateCommits.invoke(
//            parentTaskInfo.projectName,
//            parentTaskInfo.platform,
//            parentTaskInfo.projectDescription,
//            parentTaskInfo.taskQuery,
//            prefix,
//            newSuffix,
//            artifactSink.adapt(),
//        )
//            .map { (commitDetailsAndFileOpsList) ->
//                commitDetailsAndFileOpsList
//            }
//    }

    override fun prepareUpdate(
        listenResource: ArbrSubtask,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.commits.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val task = requireAttachedOrElseComplete(listenResource.parent)

        val projectFullName = require(readResource.fullName)
        val platform = require(readResource.platform)
        val description = require(readResource.description)

        val projectFiles = require(readResource.files).values
        val fileByUUID = projectFiles.associateBy { it.uuid }
        val filesByPath = projectFiles.groupBy { it.filePath.getLatestAcceptedValue()?.value }

        val taskQuery = require(task.taskQuery)
        val verbosePlan = require(task.taskVerbosePlan)

        val subtask = require(listenResource.subtask)
        val subtaskRelevantFiles = require(listenResource.subtaskRelevantFiles)

        val filesWithSummariesValue = FilePathsAndSummaries.initializeMerged(
            subtaskRelevantFiles
                .mapNotNull { (_, fileRelation) ->
                    fileRelation.file.getLatestAcceptedValue()?.resource()?.uuid?.let { fileByUUID[it] }
                }
                .map { file ->
                    val filePathValue = require(file.filePath)
                    val fileSummaryValue = require(file.summary)

                    SourcedStruct2(
                        filePathValue,
                        fileSummaryValue,
                    )
                }
        )

        return { partialObjectGraph ->
            val writeProject = partialObjectGraph.root

            taskPlanCommitDescriptionsPipelineYaml.invoke(
                projectFullName,
                platform,
                description,
                taskQuery,
                filesWithSummariesValue,
                subtask,
                artifactSink.adapt(),
            )
                .flatMap { (commitDetailsAndFileOps: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>>) ->
                    synthesize(
                        projectFullName,
                        platform,
                        description,
                        subtask,
                        verbosePlan,
                        commitDetailsAndFileOps,
                        artifactSink,
                    )
                }
                .flatMap { commitDetailsAndFileOps ->
                    if (commitDetailsAndFileOps.value.size > 3) {
                        taskBreakdownReduceCommitsApplication.invoke(
                            projectFullName,
                            platform,
                            description,
                            taskQuery,
                            commitDetailsAndFileOps,
                            artifactSink.adapt(),
                        )
                            .map { (innerCommitDetailsAndFileOps) ->
                                innerCommitDetailsAndFileOps
                            }
                    } else {
                        Mono.just(commitDetailsAndFileOps)
                    }
                }
                .map {
                    val newFiles = mutableListOf<PartialFile>()

                    val newCommits = it.containers.map { (commitMessage, diffSummary, fileOps) ->
                        val commit = PartialCommit(
                            partialObjectGraph,
                            UUID.randomUUID().toString(),
                        ).apply {
                            this.commitMessage = commitMessage
                            this.diffSummary = diffSummary
                            this.parent = PartialRef(listenResource.uuid)
                            subtaskEval =
                                null // Originating Completion status (remaining commit from subtask eval)
                        }

                        val newFileOps = fileOps.containers.mapNotNull { (fileOp, filePath) ->
                            val file = filesByPath[filePath.value]?.firstOrNull()
                            if (file == null) {
                                // Create a new file with no content
                                val newFile = PartialFile(
                                    partialObjectGraph,
                                    UUID.randomUUID().toString(),
                                ).apply {
                                    this.filePath = filePath
                                    parent = PartialRef(readResource.uuid)
                                    content = ArbrFile.Content.generated(
                                        null,
                                        filePath.generatorInfo
                                    ) // This file is known to be new, so initialize it with null contents
                                }

                                FileOpPartialHelper.fileOpPartial(
                                    partialObjectGraph = partialObjectGraph,
                                    commit = commit,
                                    file = newFile,
                                    fileOperation = fileOp,
                                    description = null, // These get added by the detail processor
                                    commitEval = PartialRef(null), // Fresh file op
                                ).also { createdFileOp ->
                                    if (createdFileOp == null) {
                                        newFiles.add(newFile)
                                    } else {
                                        newFiles.add(
                                            newFile
                                                .apply {
                                                    targetFileOfFileOp =
                                                        immutableLinkedMapOfPartials(listOf(createdFileOp))
                                                }
                                        )
                                    }
                                }
                            } else {
                                FileOpPartialHelper.fileOpPartial(
                                    partialObjectGraph = partialObjectGraph,
                                    commit = commit,
                                    file = file,
                                    fileOperation = fileOp,
                                    description = null, // These get added by the detail processor
                                    commitEval = PartialRef(null), // Fresh file op
                                ) // targetFileOfFileOp on File?
                            }
                        }

                        commit.apply {
                            this.fileOps = immutableLinkedMapOfPartials(newFileOps)
                        }
                    }

                    if (newFiles.isNotEmpty()) {
                        logger.info("Subtask breakdown creating new files: ${newFiles.joinToString(", ") { it.filePath?.value ?: "" }}")
                    }

                    partialObjectGraph.get<PartialSubtask>(listenResource.uuid)!!.commits =
                        immutableLinkedMapOfPartials(newCommits)
                    writeProject.files = (writeProject.files ?: ImmutableLinkedMap()).updatingFrom(
                        immutableLinkedMapOfPartials(newFiles)
                    )
                }
                .then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrSubtask,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val commits = requireLatestChildren(listenResource.commits.items).values
        if (commits.isEmpty()) {
            throw PostConditionFailedException("No commits after planning subtask")
        }

        commits.forEach { commit ->
            requireLatest(commit.commitMessage)
            requireLatest(commit.diffSummary)
            val fileOps = requireLatestChildren(commit.fileOps.items)
            if (fileOps.isEmpty()) {
                throw PostConditionFailedException("No file ops in commit")
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowProjectSubtaskToCommitProcessor::class.java)

        private const val commitSynthesisLimit = 100
    }
}