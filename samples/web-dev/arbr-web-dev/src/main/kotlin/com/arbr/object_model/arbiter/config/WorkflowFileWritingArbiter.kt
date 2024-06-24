package com.arbr.object_model.arbiter.config

//import com.arbr.api_server_base.service.file_system.client.VolumeStateFactory
//import com.arbr.api_server_base.service.github.GitHubCredentialsUtils
//import com.arbr.api_server_base.service.github.GitHubCredentialsUtils.writeGitHubCredentials
//import com.arbr.engine.util.FileContentUtils
//import com.arbr.model_loader.indents.IndentAlignmentService
//import com.arbr.object_model.core.partial.PartialCommitRelevantFile
//import com.arbr.object_model.core.partial.PartialFileOp
//import com.arbr.object_model.core.partial.PartialFileSegment
//import com.arbr.object_model.core.partial.PartialProject
//import com.arbr.object_model.core.partial.PartialSubtaskRelevantFile
//import com.arbr.object_model.core.partial.PartialTaskRelevantFile
//import com.arbr.object_model.core.resource.ArbrCommitRelevantFile
//import com.arbr.object_model.core.resource.ArbrFile
//import com.arbr.object_model.core.resource.ArbrFileOp
//import com.arbr.object_model.core.resource.ArbrFileSegment
//import com.arbr.object_model.core.resource.ArbrProject
//import com.arbr.object_model.core.resource.ArbrSubtaskRelevantFile
//import com.arbr.object_model.core.resource.ArbrTaskRelevantFile
//import com.arbr.object_model.core.resource.field.ArbrFileContentValue
//import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
//import com.arbr.object_model.core.resource.field.ArbrFileIsBinaryValue
//import com.arbr.object_model.core.resource.field.ArbrFileSourceLanguageValue
//import com.arbr.object_model.core.resource.field.ArbrFileSummaryValue
//import com.arbr.object_model.core.types.ArbrForeignKey
//import com.arbr.object_model.engine.arbiter.base.AbstractWorkflowFileArbiter
//import com.arbr.og.object_model.common.model.Proposal
//import com.arbr.platform.object_graph.impl.ObjectRef
//import com.arbr.og_engine.core.WorkflowResourceModel
//import com.arbr.og_engine.file_system.TaskInstructionDeprecated
//import com.arbr.og_engine.file_system.VolumeState
//import com.arbr.util_common.invariants.Invariants
//import com.arbr.util_common.reactor.single
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//import reactor.core.scheduler.Schedulers
//
//@Component
//class WorkflowFileWritingArbiter(
//    private val volumeStateFactory: VolumeStateFactory,
//    private val indentAlignmentService: IndentAlignmentService,
//) : AbstractWorkflowFileArbiter() {
//    /**
//     * Test if a file edit is disallowed by its path. Disallowed edits include: .gitignore Any file
//     * matched by gitignore
//     */
//    private fun fileEditIsDisallowed(
//        volumeState: VolumeState,
//        filePath: String,
//    ): Mono<Boolean> {
//        if (filePath.contains(".gitignore")) {
//            return Mono.just(true)
//        }
//
//        if (badExtensions.any { filePath.endsWith(it) }) {
//            return Mono.just(true)
//        }
//
//        return volumeState.execReadInProject("git check-ignore $filePath").map { shout ->
//            shout.stdout.any { line -> line.contains(filePath) }
//        }
//    }
//
//    /**
//     * Process write for content update and return whether the write was accepted
//     *
//     * This is the thing that actually triggers writes to disk
//     */
//    private fun handleNewContent(
//        workflowHandleId: String,
//        filePath: String,
//        oldContent: ArbrFileContentValue?,
//        newContent: ArbrFileContentValue?,
//        volumeState: VolumeState,
//    ): Mono<Boolean> {
//        // pre-condition: oldContent != newContent
//        val taskInstruction =
//            when {
//                oldContent?.value == null -> TaskInstructionDeprecated.CreateFile(filePath)
//                newContent?.value == null -> TaskInstructionDeprecated.DeleteFile(filePath)
//                else -> TaskInstructionDeprecated.EditFile(filePath)
//            }
//
//        return fileEditIsDisallowed(volumeState, filePath).flatMap { isDisallowed ->
//            val fileExistsMono =
//                volumeState
//                    .listFilePaths()
//                    .filter {
//                        it.filePath == filePath // TODO: does this need normalization?
//                    }
//                    .next()
//                    .map { true }
//                    .defaultIfEmpty(false)
//
//            fileExistsMono
//                .flatMap { fileExists ->
//                    val isInitialLoad = fileExists && oldContent == null
//
//                    if (isInitialLoad) {
//                        // Always accept initial content writes which represent initial content loading. But
//                        // no need to
//                        // actually write
//                        logger.info("Performing initial load of $filePath")
//                        Mono.just(true)
//                    } else if (isDisallowed) {
//                        logger.warn("Ignoring disallowed write to $filePath")
//                        Mono.just(false)
//                    } else {
//                        val newContentValue =
//                            newContent?.value?.let { contents ->
//                                FileContentUtils.normalizeFileContent(contents)
//                            }
//                        logger.info(
//                            "Writing ${newContentValue?.length} bytes to volume for $filePath as ${taskInstruction::class.java.simpleName}"
//                        )
//                        volumeState
//                            .evolve(
//                                taskInstruction,
//                                newContentValue,
//                            )
//                            .single("File write gave empty")
//                            .thenReturn(true)
//                            .onErrorReturn(false)
//                    }
//                }
//                .flatMap { resultFlag ->
//                    Mono.fromCallable {
//                        indentAlignmentService.fileContentWasUpdated(
//                            workflowHandleId,
//                            filePath,
//                            oldContent?.value,
//                            newContent?.value,
//                        )
//                    }
//                        .subscribeOn(Schedulers.boundedElastic())
//                        .thenReturn(resultFlag)
//                }
//        }
//    }
//
//    override fun arbitrate(
//        workflowResourceModel: WorkflowResourceModel,
//        resource: ArbrFile,
//        contentUpdate: Proposal<ArbrFileContentValue>?,
//        filePathUpdate: Proposal<ArbrFileFilePathValue>?,
//        isBinaryUpdate: Proposal<ArbrFileIsBinaryValue>?,
//        sourceLanguageUpdate: Proposal<ArbrFileSourceLanguageValue>?,
//        summaryUpdate: Proposal<ArbrFileSummaryValue>?,
//        parentUpdate: Proposal<ObjectRef<out ArbrProject, PartialProject, ArbrForeignKey>>?,
//        taskRelevantFilesItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrTaskRelevantFile, PartialTaskRelevantFile, ArbrForeignKey>>>?,
//        subtaskRelevantFilesItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrSubtaskRelevantFile, PartialSubtaskRelevantFile, ArbrForeignKey>>>?,
//        commitRelevantFilesItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrCommitRelevantFile, PartialCommitRelevantFile, ArbrForeignKey>>>?,
//        implementedFileOfFileOpItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileOp, PartialFileOp, ArbrForeignKey>>>?,
//        targetFileOfFileOpItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileOp, PartialFileOp, ArbrForeignKey>>>?,
//        fileSegmentsItemsUpdate: Proposal<Map<String, ObjectRef<out ArbrFileSegment, PartialFileSegment, ArbrForeignKey>>>?
//    ): Mono<Void> {
//        val projectFullName =
//            resource.parent.getLatestAcceptedValue()?.resource()?.fullName?.getLatestAcceptedValue()?.value
//                ?: return Mono.empty()
//
//        if (filePathUpdate != null) {
//            val newFilePath = filePathUpdate.proposedValue?.value
//            val oldFilePath = filePathUpdate.acceptedValue?.value
//
//            if (newFilePath != oldFilePath && oldFilePath != null) {
//                logger.warn("Unhandled case: file path update from $oldFilePath to $newFilePath")
//                return Mono.error(Exception("Unhandled case: file path update"))
//            }
//
//            filePathUpdate.accept()
//        }
//
//        val acceptContentMono =
//            if (contentUpdate == null) {
//                Mono.just(true)
//            } else {
//                val filePath =
//                    resource.filePath.getLatestAcceptedValue()?.value
//                        ?: run {
//                            logger.warn("No file path for arbiter to update file")
//                            Invariants.check { require -> require(false) }
//                            return Mono.empty()
//                        }
//
//                val acceptedContent = contentUpdate.acceptedValue
//                val proposedContent = contentUpdate.proposedValue
//
//                logger.info(
//                    "Arbitrating file content update: $filePath from ${acceptedContent?.value?.length} to ${proposedContent?.value?.length} bytes"
//                )
//
//                // TODO: Get from DB?
//                GitHubCredentialsUtils.fromContext().flatMap { gitHubCredentials ->
//                    Mono.defer {
//                        // Acquire a fresh volume state
//                        // This feels a bit like cheating the state model, but we only care about the latest
//                        // state here anyway
//                        val volumeState =
//                            volumeStateFactory.gitRemoteVolumeState(
//                                projectFullName,
//                                workflowResourceModel.workflowHandleId,
//                            )
//
//                        handleNewContent(
//                            workflowResourceModel.workflowHandleId,
//                            filePath,
//                            acceptedContent,
//                            proposedContent,
//                            volumeState,
//                        )
//                    }.writeGitHubCredentials(
//                        gitHubCredentials.username,
//                        gitHubCredentials.accessToken,
//                    )
//                }
//            }
//
//        return acceptContentMono
//            .map { acceptContent ->
//                if (acceptContent) {
//                    contentUpdate?.accept()
//                } else {
//                    contentUpdate?.reject()
//                }
//
//                val proposals =
//                    listOfNotNull(
//                        parentUpdate,
//                        // Handled in block above
//                        // filePathUpdate,
//                        isBinaryUpdate,
//                        sourceLanguageUpdate,
//                        summaryUpdate,
//                        fileSegmentsItemsUpdate,
//                        targetFileOfFileOpItemsUpdate,
//                        implementedFileOfFileOpItemsUpdate,
//                        commitRelevantFilesItemsUpdate,
//                        subtaskRelevantFilesItemsUpdate,
//                        taskRelevantFilesItemsUpdate,
//                    )
//
//                proposals.forEach { proposal -> proposal.accept() }
//            }
//            .then()
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(WorkflowFileWritingArbiter::class.java)
//
//        private val badExtensions =
//            listOf(
//                ".jpg",
//                ".jpeg",
//                ".png",
//                ".ico",
//                ".conf",
//                "Dockerfile",
//            )
//    }
//}
