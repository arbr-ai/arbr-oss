package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.workflow.model.FileOperation
import com.arbr.engine.services.differential_content.diff_alignment.DiffApplicatorService
import com.arbr.engine.util.FileContentUtils
import com.arbr.util.adapt
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrCommitCommitMessageValue
import com.arbr.object_model.core.resource.field.ArbrCommitDiffSummaryValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileOpFileOperationValue
import com.arbr.object_model.core.resource.field.ArbrProjectFullNameValue
import com.arbr.object_model.core.resource.field.ArbrSubtaskSubtaskValue
import com.arbr.object_model.core.resource.field.ArbrTaskTaskQueryValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.collections.NestedObjectListType2
import com.arbr.og.object_model.common.model.collections.NestedObjectListType3
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.CommitDetailsAndFileOps
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePaths
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePathsWithDescriptions
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Specific treatment of edits to package.json.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-op-impl-diff-package-json", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileOpDiffPackageJsonImplementationProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val diffApplicatorService: DiffApplicatorService,
) : ArbrResourceFunction<
        ArbrFileOp, PartialFileOp,
        ArbrProject, PartialProject,
        ArbrFile, PartialFile,
        >(objectModelParser) {
    private val localCodeEditPackageJsonApplication = promptLibrary.localCodeEditPackageJsonApplication

    override val name: String
        get() = "file-op-impl-diff-package-json"
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrFile>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrFileOp,
        readResource: ArbrProject,
        writeResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): (PartialObjectGraph<ArbrFile, PartialFile, ArbrForeignKey>) -> Mono<Void> {
        val parentCommit = requireAttachedOrElseComplete(listenResource.parent)
        val subtaskResource = requireAttachedOrElseComplete(parentCommit.parent)
        val targetFile = requireAttachedOrElseComplete(listenResource.targetFile)
        val targetFilePath = require(targetFile.filePath)

        val task = requireAttachedOrElseComplete(subtaskResource.parent)

        val projectFullName = require(readResource.fullName)

        val taskQuery = require(task.taskQuery)

        val fileOp = require(listenResource.fileOperation)
        val fileOpValue = fileOp.value ?: ""

        val fileOpEnumValue = run {
            // TODO: Validate at the source
            val rawFileOpEnumValue = FileOperation.parseLine(fileOpValue)
                ?: run {
                    logger.error("File Operation uninterpretable: {}", fileOpValue)

                    return { partialObjectGraph ->
                        // Ignore the operation by setting the implemented file to the target file
                        partialObjectGraph.get<PartialFileOp>(listenResource.uuid)!!.implementedFile =
                            PartialRef(targetFile.uuid)
                        partialObjectGraph.root.content =
                            partialObjectGraph.root.content ?: ArbrFile.Content.constant(null)

                        Mono.empty()
                    }
                }

            // TODO: Split kinds of ops into different processors
            val existingFileContent = targetFile.content.getLatestAcceptedValue()?.value
            if (rawFileOpEnumValue == FileOperation.CREATE_FILE && existingFileContent != null) {
                // Create file which was previously implemented - edit instead
                // Note: not currently considering weird edge cases like edit->delete->create
                FileOperation.EDIT_FILE
            } else if (rawFileOpEnumValue == FileOperation.EDIT_FILE && existingFileContent == null) {
                FileOperation.CREATE_FILE
            } else {
                rawFileOpEnumValue
            }
        }

        // This processor handles edits on package.json only
        if (!FileContentUtils.fileIsPackageJson(targetFilePath.value) || fileOpEnumValue != FileOperation.EDIT_FILE) {
            throw OperationCompleteException()
        }

        val subtaskCommit = requireAttached(listenResource.parent) // Maybe orElseComplete here
        val subtaskQuery = require(subtaskResource.subtask)
        val subtaskCommits = require(subtaskResource.commits).values

        val commitDetailsAndFileOpsList: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>> =
            CommitDetailsAndFileOps.initializeMerged(
                subtaskCommits.map { commit ->
                    SourcedStruct3(
                        require(commit.commitMessage),
                        require(commit.diffSummary),
                        FileOperationsAndTargetFilePaths.initializeMerged(
                            require(commit.fileOps).values
                                .map {
                                    SourcedStruct2(
                                        require(it.fileOperation),
                                        require(requireAttached(it.targetFile).filePath),
                                    )
                                }
                        )
                    )
                }
            )

        val commitMessage = require(subtaskCommit.commitMessage)

        fun checkPriorOpsOnSameFilePathImplemented() {
            // The base content for generating the implementation of the new file should be the latest planned
            // implementation prior to this file op in the commit history
            // Since impls are proposed to the target file as they're planned, this will be the target file content at
            // planning time
            for (priorSubtaskResource in require(task.subtasks).values) {
                for (commit in require(priorSubtaskResource.commits).values) {
                    val fileOperations = require(commit.fileOps).values
                    for (priorFileOp in fileOperations) {
                        if (listenResource.uuid == priorFileOp.uuid) {
                            // We've reached this resource
                            return
                        }

                        val priorFileOpValue = require(priorFileOp.fileOperation).value
                        if (priorFileOpValue != null && FileOperation.parseLine(priorFileOpValue) == FileOperation.DELETE_FILE) {
                            // No need to check deletes
                            continue
                        }

                        val priorFileOpOnFileExists =
                            targetFilePath.value == require(requireAttached(priorFileOp.targetFile).filePath).value
                        if (priorFileOpOnFileExists) {
                            // Require an implementation
                            require(priorFileOp.implementedFile)
                        }
                    }
                }
            }
        }
        checkPriorOpsOnSameFilePathImplemented()

        return editFile(
            volumeState.workflowHandleId,
            projectFullName,
            taskQuery,
            subtaskQuery,
            commitDetailsAndFileOpsList,
            commitMessage,
            targetFilePath,
            artifactSink,
            targetFile,
            listenResource
        )
    }

    private fun editFile(
        workflowHandleId: String,
        projectFullName: ArbrProjectFullNameValue,
        taskQuery: ArbrTaskTaskQueryValue,
        subtask: ArbrSubtaskSubtaskValue,
        commitDetailsAndFileOpsList: NestedObjectListType3.Value<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableT, Dim.VariableT>>, Shape.Product<Dim.VariableC, Shape.Sum<Dim.VariableF, Dim.VariableF>>, ArbrCommitCommitMessageValue, ArbrCommitDiffSummaryValue, NestedObjectListType2.Value<String?, String, Dim.VariableT, Dim.VariableF, Dim.VariableT, Dim.VariableF, ArbrFileOpFileOperationValue, ArbrFileFilePathValue>>,
        commitMessage: ArbrCommitCommitMessageValue,
        targetFilePath: ArbrFileFilePathValue,
        artifactSink: FluxSink<Artifact>,
        targetFile: ArbrFile,
        resource: ArbrFileOp,
    ): (PartialObjectGraph<ArbrFile, PartialFile, ArbrForeignKey>) -> Mono<Void> {
        val targetFileSummary = require(targetFile.summary)
        val targetFileContent = require(targetFile.content)
        val fileOp = require(resource.fileOperation)
        val description = require(resource.description)

        return { partialObjectGraph ->
            val writeFile = partialObjectGraph.root
            val writeFileOp = partialObjectGraph.get<PartialFileOp>(resource.uuid)!!

            logger.info("Actually implementing edit file op ${resource.uuid}")
            localCodeEditPackageJsonApplication.invoke(
                projectFullName,
                taskQuery,
                subtask,
                commitDetailsAndFileOpsList,
                commitMessage,
                FileOperationsAndTargetFilePathsWithDescriptions.initializeMerged(
                    listOf(
                        SourcedStruct3(
                            fileOp,
                            targetFilePath,
                            description,
                        )
                    )
                ),
                targetFileSummary,
                targetFileContent,
                artifactSink.adapt(),
            )
                .flatMap { (fileContentDiff) ->
                    diffApplicatorService.extractAndApplyAlignedDiff(
                        workflowHandleId,
                        targetFilePath.value,
                        targetFileContent.value ?: "",
                        fileContentDiff.value ?: "",
                    )
                        .doOnSubscribe {
                            logger.info("Got raw output for diff:\n${fileContentDiff.value}")
                        }
                        .doOnNext {
                            logger.info("Applied diff:\n${it}")
                        }
                }
                .doOnSubscribe {
                    logger.info("Will implement [edit_file ${targetFilePath.value}] for commit ${commitMessage.value}")
                }
                .doOnNext {
                    logger.info("Would write edit to ${targetFilePath.value} with ${it.length} bytes")
                }
                .map {

                    // Freeze base content at planning point
                    writeFileOp.baseFileContent = ArbrFileOp.BaseFileContent.computed(
                        targetFileContent.value,
                        targetFileContent.generatorInfo,
                    )

                    if (it.trim() == targetFile.content.getLatestAcceptedValue()?.value?.trim()) {
                        // Skip update
                        logger.info("Skipping update for equal content (edit)")
                    } else {
                        writeFile.content = ArbrFile.Content.materialized(it)
                    }

                    // Attach to the file op
                    writeFileOp.implementedFile = PartialRef(targetFile.uuid)

                    it
                }
                .then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileOp,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ) {
        requireLatest(
            listenResource.implementedFile,
            message = "No implemented file post file op",
        )
        val file = requireLatestAttached(listenResource.targetFile)
        requireLatest(file.content, message = "No file content post file op")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileOpDiffPackageJsonImplementationProcessor::class.java)
    }
}
