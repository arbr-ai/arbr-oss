package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.types.ArbrForeignKey
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
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePaths
import com.arbr.prompt_library.util.FilePathsAndContents
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Adds more detail to a file op.
 *
 * This operation works by looking "up the tree". It is centered on the file op in order to handle new additions,
 * but looks to its parent to compute the descriptions for all of its siblings.
 * This has the added value of keeping sibling descriptions up to date.
 * But it also means there is a race to compute these descriptions between each file op and its siblings, so in the
 * worst case there might be some duplicate work.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-op-detail", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileOpDetailProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrFileOp, PartialFileOp,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject, // Need to write to the project in order to write to files
        >(objectModelParser) {
    private val fileOpsDetailApplication = promptLibrary.fileOpsDetailApplication

    override val name: String
        get() = "file-op-detail"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val fileOpDetailLockMap = ConcurrentHashMap<String, Mono<Void>>()

    override fun selectReadResource(listenResource: ArbrFileOp): ArbrProject {
        val commit = requireAttachedOrElseComplete(listenResource.parent)
        val subtask = requireAttachedOrElseComplete(commit.parent)
        val task = requireAttachedOrElseComplete(subtask.parent)
        return requireAttachedOrElseComplete(task.parent)
    }

    override fun selectWriteResource(listenResource: ArbrFileOp): ArbrProject {
        val commit = requireAttachedOrElseComplete(listenResource.parent)
        val subtask = requireAttachedOrElseComplete(commit.parent)
        val task = requireAttachedOrElseComplete(subtask.parent)
        return requireAttachedOrElseComplete(task.parent)
    }

    override fun prepareUpdate(
        listenResource: ArbrFileOp,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.description.getLatestValue() != null) {
            // All done
            throw OperationCompleteException()
        }

        val commit = requireAttached(listenResource.parent)

        val subtaskResource = requireAttachedOrElseComplete(commit.parent)
        val task = requireAttachedOrElseComplete(subtaskResource.parent)

        val projectFullName = require(readResource.fullName)
        val taskQuery = require(task.taskQuery)

        // Require relevant files!
        val relevantFiles = require(commit.commitRelevantFiles)

        val commitMessage = require(commit.commitMessage)
        val diffSummary = require(commit.diffSummary)

        val subtask = require(subtaskResource.subtask)

        val projectFiles = require(readResource.files)

        val relevantFileContents = FilePathsAndContents.initializeMerged(
            relevantFiles.mapNotNull { (_, relevantFile) ->
                val file = relevantFile.file.getLatestAcceptedValue()?.resource()?.uuid?.let { projectFiles[it] }
                val filePath = file?.filePath?.getLatestAcceptedValue()
                val fileContent = file?.content?.getLatestAcceptedValue()
                if (file == null || filePath == null || fileContent == null) {
                    null
                } else {
                    SourcedStruct2(filePath, fileContent)
                }
            }
        )

        // Start with shells of file ops
        val allCommitFileOps = require(commit.fileOps).values.toList()

        val fileOperationsAndTargetFilePaths = FileOperationsAndTargetFilePaths.initializeMerged(
            allCommitFileOps.map { fileOp ->
                val filePath = require(requireAttached(fileOp.targetFile).filePath)

                SourcedStruct2(require(fileOp.fileOperation), filePath)
            }
        )

        val currentFileOpsMap = allCommitFileOps.associateBy { fileOp ->
            val filePath = require(requireAttached(fileOp.targetFile).filePath)
            filePath.value
        }

        val filesByPath =
            projectFiles.values.groupBy { file ->
                val filePathValue = require(file.filePath)

                filePathValue.value
            }

        // Deduplicate operations on the basis of parent + ordered sibling UUIDs
        val fileOpDetailsLockKey = writeResource.uuid + ":" + allCommitFileOps.joinToString(":") { it.uuid }

        return { partialObjectGraph ->
            val writeCommit = partialObjectGraph.get<PartialCommit>(commit.uuid)!!

            fileOpDetailLockMap.computeIfAbsent(fileOpDetailsLockKey) {
                val newCommitMono = fileOpsDetailApplication.invoke(
                    projectFullName,
                    taskQuery,
                    subtask,
                    commitMessage,
                    diffSummary,
                    relevantFileContents,
                    fileOperationsAndTargetFilePaths,
                    artifactSink.adapt(),
                ).map { (fileOps) ->
                    // We need to match the returned file ops against the commit's file ops by name

                    fileOps.containers
                        .distinctBy { it.t2.value } // Distinct on file path
                        .mapNotNull { (fileOp, filePath, description) ->
                            val currentMatchingFileOpView = currentFileOpsMap[filePath.value]
                            val currentMatchingFileOp = currentMatchingFileOpView?.let {
                                partialObjectGraph.get<PartialFileOp>(it.uuid)
                            }

                            if (currentMatchingFileOp == null) {
                                val matchingFile = filesByPath[filePath.value]?.lastOrNull()
                                if (matchingFile == null) {
                                    // Might want to error? File ops might end up without descriptions
                                    // TODO: Ensure file ops always end up with descriptions here
                                    logger.warn("No file matching ${filePath.value} from suggested file op")
                                    null
                                } else {
                                    FileOpPartialHelper.fileOpPartial(
                                        partialObjectGraph,
                                        writeCommit,
                                        matchingFile,
                                        fileOp,
                                        description,
                                        commitEval = PartialRef(null), // Fresh file op
                                    )
                                }
                            } else {
                                currentMatchingFileOp.description = description
                                currentMatchingFileOp
                            }
                        }
                }
                    .single()
                    .map { fileOps ->

                        writeCommit.fileOps = immutableLinkedMapOfPartials(fileOps)
                    }

                Mono.defer {
                    logger.info("Adding file op details to ${writeResource.uuid}")
                    newCommitMono.then()
                }.cache(
                    /* ttlForValue = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
                    /* ttlForError = */ { Duration.ZERO }, // Do not cache errors since we expect retries
                    /* ttlForEmpty = */ { Duration.ofMinutes(5L) }, // Cache values (empty) for a reasonably long time
                )
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileOp,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val commit = listenResource.parent.getLatestValue()?.resource()
            ?: return

        val fileOps = requireLatestChildren(commit.fileOps.items)
        if (fileOps.isEmpty()) {
            throw PostConditionFailedException("No file ops on commit after details")
        }

        // Require that all fileOps have descriptions
        fileOps.forEach { (_, fileOp) ->
            requireLatest(fileOp.targetFile)
            requireLatest(fileOp.fileOperation)
            requireLatest(fileOp.description)
            requireLatest(fileOp.parent)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileOpDetailProcessor::class.java)
    }
}