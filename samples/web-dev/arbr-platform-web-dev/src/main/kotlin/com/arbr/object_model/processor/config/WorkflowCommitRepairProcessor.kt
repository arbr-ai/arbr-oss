package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.FileOpPartialHelper
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialCommitEval
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrCommitEvalErrorContentValue
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileOpDescriptionValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
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
import com.arbr.prompt_library.util.FilePaths
import com.arbr.prompt_library.util.FilePathsAndContents
import com.arbr.prompt_library.util.FilePathsContainer
import com.arbr.prompt_library.util.FilePathsValue
import com.arbr.relational_prompting.services.ai_application.application.AiApplication
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Build result with failures -> repair commits
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.commit-repair", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowCommitRepairProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrCommitEval, PartialCommitEval,
        ArbrProject, PartialProject,
        ArbrProject, PartialProject, // Needs commit and target file
        >(objectModelParser) {
    private val repairFileSelectApplication = promptLibrary.repairFileSelectApplication
    private val repairAddFileOpDescriptionsApplication = promptLibrary.repairAddFileOpDescriptionsApplication

    override val name: String
        get() = "commit-repair"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrProject>
        get() = cls()

    private val editFileOp = ArbrFileOp.FileOperation.constant("edit_file")

    private fun matchFiles(
        projectFiles: List<Pair<String, SourcedStruct2<ArbrFileFilePathValue, ArbrFileContentValue>>>,
        fuzzyFileNames: List<FilePathsContainer>,
    ): Pair<List<SourcedStruct2<ArbrFileFilePathValue, ArbrFileContentValue>>, List<String>> {
        val fuzzyFilePathStrings = fuzzyFileNames.map { it.t1.value }.toSet()
        val usedFuzzyFilePathStrings = mutableSetOf<String>()

        val matches = projectFiles.mapNotNull { (filePath, container) ->
            val matchingFileName = fuzzyFilePathStrings.firstOrNull { filePath in it }
            if (matchingFileName == null) {
                null
            } else {
                usedFuzzyFilePathStrings.add(matchingFileName)
                container
            }
        }

        val notFound = (fuzzyFilePathStrings - usedFuzzyFilePathStrings).toList()

        return matches to notFound
    }

    private fun getRepairFileOpDescription(
        errorContent: ArbrCommitEvalErrorContentValue,
        filePathsNotFound: FilePathsValue,
        otherFiles: List<SourcedStruct2<ArbrFileFilePathValue, ArbrFileContentValue>>,
        targetFiles: List<SourcedStruct2<ArbrFileFilePathValue, ArbrFileContentValue>>,
        targetFileIndex: Int,
        artifactSink: FluxSink<Artifact>,
    ): Mono<ArbrFileOpDescriptionValue> {
        val targetFile = targetFiles[targetFileIndex]

        val maxContextFiles = 2
        val otherTargetFiles = targetFiles.filterIndexed { i, _ ->
            i != targetFileIndex
        }
        val contextFiles = (otherFiles + otherTargetFiles).takeLast(maxContextFiles)


        return repairAddFileOpDescriptionsApplication.invoke(
            errorContent,
            filePathsNotFound,
            FilePathsAndContents.initializeMerged(contextFiles),
            FileOperationsAndTargetFilePaths.initializeMerged(
                listOf(
                    SourcedStruct2(
                        editFileOp,
                        targetFile.t1
                    ),
                )
            ),
            targetFile.t2,
            artifactSink.adapt(),
        )
            .contextWrite { ctx ->
                ctx.put(AiApplication.ALLOW_CACHED_COMPLETIONS, false)
            }
            .map { (description) ->
                description
            }
    }


    override fun prepareUpdate(
        listenResource: ArbrCommitEval,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrProject, PartialProject, ArbrForeignKey>) -> Mono<Void> {
        val buildSuccess = require(listenResource.buildSuccess)
        val renderSuccess = require(listenResource.renderSuccess)

        // For now, treat build and render failures the same. In the future, we can dig into them separately
        val success = buildSuccess.value && renderSuccess.value

        if (success) {
            // The build was successful, no need to repair
            throw OperationCompleteException()
        }

        val commit = requireAttachedOrElseComplete(listenResource.parent)

        val errorContent = require(listenResource.errorContent)

        val projectFiles = require(readResource.files).values

        val filePathsValue = FilePaths.initializeMerged(
            projectFiles.map { SourcedStruct1(require(it.filePath)) }
        )

        val projectFullName = require(readResource.fullName)
        val projectPlatform = require(readResource.platform)
        val projectDescription = require(readResource.description)

        val projectFileContentsByPath: List<Pair<String, SourcedStruct2<ArbrFileFilePathValue, ArbrFileContentValue>>> = projectFiles.map {
            val filePath = require(it.filePath)
            filePath.value to SourcedStruct2(filePath, require(it.content))
        }
        val projectFileByPath = projectFiles.groupBy {
            require(it.filePath).value
        }

        return { partialObjectGraph ->
            val writeCommit = partialObjectGraph.get<PartialCommit>(commit.uuid)!!

            logger.info("Attempting repair on commit: ${commit.commitMessage}")

            repairFileSelectApplication.invoke(
                projectFullName,
                projectPlatform,
                projectDescription,
                filePathsValue,
                errorContent,
                artifactSink.adapt(),
            )
                .contextWrite { ctx ->
                    ctx.put(AiApplication.ALLOW_CACHED_COMPLETIONS, false)
                }
                .flatMap { (targetFiles, otherFiles) ->
                    val targetFileContainers = targetFiles.containers
                    val otherFileContainers = otherFiles.containers

                    val (matchedTargetFileContainers, unmatchedTargetFilePaths) = matchFiles(
                        projectFileContentsByPath,
                        targetFileContainers,
                    )
                    val (matchedOtherFileContainers, unmatchedOtherFilePaths) = matchFiles(
                        projectFileContentsByPath,
                        otherFileContainers,
                    )

                    val filesNotFound = unmatchedTargetFilePaths + unmatchedOtherFilePaths
                    val filePathsNotFound = FilePaths.initializeMerged(
                        filesNotFound.map {
                            SourcedStruct1(
                                ArbrFile.FilePath.generated(
                                    it,
                                    targetFiles.generatorInfo,
                                )
                            )
                        }
                    )

                    Flux.fromIterable(matchedTargetFileContainers.indices)
                        .concatMap({ targetFileIndex ->
                            getRepairFileOpDescription(
                                errorContent,
                                filePathsNotFound,
                                matchedOtherFileContainers,
                                matchedTargetFileContainers,
                                targetFileIndex,
                                artifactSink,
                            ).map { targetFileIndex to it }
                        }, 8)
                        .mapNotNull<PartialFileOp> { (targetFileIndex, description) ->
                            val targetFile = matchedTargetFileContainers[targetFileIndex]
                            FileOpPartialHelper.fileOpPartial(
                                partialObjectGraph,
                                writeCommit,
                                projectFileByPath[targetFile.t1.value]!!.last(),
                                editFileOp,
                                description,
                                commitEval = PartialRef(null), // TODO: Probably want to use this to assign empty dependencies on any file seg ops similar to Commit Completion
                            )
                        }
                        .collectList()
                        .map { newFileOps ->
                            for (fileOp in newFileOps) {
                                logger.info("Adding repair file op to ${writeCommit.uuid} : ${fileOp.uuid}")
                            }

                            writeCommit.fileOps =
                                (writeCommit.fileOps ?: ImmutableLinkedMap()).updatingFrom(
                                    immutableLinkedMapOfPartials(newFileOps)
                                )
                        }
                        .then()
                }
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrCommitEval,
        readResource: ArbrProject,
        writeResource: ArbrProject,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        val commit = requireAttached(listenResource.parent)
        acquire(commit.fileOps.items)

        // Commit may target any file
        writeResource.files.items.getLatestAcceptedValue()?.values?.forEach { ref ->
            ref.resource()?.targetFileOfFileOp?.items?.let(acquire)
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrCommitEval,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrProject,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val success = requireLatest(listenResource.buildSuccess)
        if (success.value) {
            // No need to check
            return
        }

        val commit = requireLatestAttached(listenResource.parent)
        val fileOps = requireLatestChildren(commit.fileOps.items)
        if (fileOps.isEmpty()) {
            throw PostConditionFailedException("Repair added no file ops")
        }

        fileOps.forEach { (_, fileOp) ->
            requireLatest(fileOp.fileOperation)
            requireLatest(fileOp.targetFile)
            requireLatest(fileOp.description)
        }

        // Check that some file op needs to be implemented after a repair plan
        val anyNeedsImplementation = fileOps.any { (_, fileOp) ->
            fileOp.implementedFile.getLatestValue() == null
        }
        if (!anyNeedsImplementation) {
            throw PostConditionFailedException("No file op needs implementation after repair plan")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowCommitRepairProcessor::class.java)
    }
}