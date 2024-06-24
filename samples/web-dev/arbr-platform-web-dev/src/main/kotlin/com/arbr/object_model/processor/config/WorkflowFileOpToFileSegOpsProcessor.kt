package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.workflow.model.FileOperation
import com.arbr.util.adapt
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.partial.PartialFileSegmentOp
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegmentOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrFileSegmentContentTypeValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpContentTypeValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpElementIndexValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpNameValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpOperationValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpRuleNameValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentRuleNameValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.values.collections.SourcedStruct4
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.core.RequiredValueMissingException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FileSegments
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.*

/**
 * Adds file segment ops to a file op.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-op-to-file-seg-ops", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileOpToFileSegOpsProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrFileOp, PartialFileOp,
        ArbrProject, PartialProject,
        ArbrFileOp, PartialFileOp,
        >(objectModelParser) {
    private val fileOpNewToFileSegOps = promptLibrary.fileOpNewToFileSegOps
    private val fileOpEditToFileSegOps = promptLibrary.fileOpEditToFileSegOps

    override val name: String
        get() = "file-op-to-file-seg-ops"
    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrFileOp>
        get() = cls()

    private fun detachedFileSegmentOp(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        resource: PartialFileOp,
        opValue: ArbrFileSegmentOpOperationValue,
        contentType: ArbrFileSegmentOpContentTypeValue,
        ruleName: ArbrFileSegmentOpRuleNameValue,
        fileSegName: ArbrFileSegmentOpNameValue,
        elementIndex: ArbrFileSegmentOpElementIndexValue,
        description: ArbrFileSegmentOpDescriptionValue,
    ): PartialFileSegmentOp {
        return PartialFileSegmentOp(
            partialObjectGraph,
            UUID.randomUUID().toString(),
        ).apply {
            operation = opValue
            this.contentType = contentType
            this.ruleName = ruleName
            name = fileSegName
            this.elementIndex = elementIndex
            this.description = description
            content = null

            parent = PartialRef(resource.uuid)
            implementedFileSegment = null
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrFileOp,
        readResource: ArbrProject,
        writeResource: ArbrFileOp,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrFileOp, PartialFileOp, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.fileSegmentOps.items.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        if (listenResource.parent.getLatestAcceptedValue() == null) {
            logger.error("Violator: ${listenResource.uuid}")
        }
        Invariants.check { require ->
            require(listenResource.parent.getLatestAcceptedValue() != null)
        }

        val parentCommit = requireAttachedOrElseComplete(listenResource.parent)
        val subtaskResource = requireAttachedOrElseComplete(parentCommit.parent)
        val subtask = require(subtaskResource.subtask)
        val targetFile = requireAttachedOrElseComplete(listenResource.targetFile)
        val task = requireAttachedOrElseComplete(subtaskResource.parent)

        val projectFullName = require(readResource.fullName)
        val taskQuery = require(task.taskQuery)

        val commitMessage = require(parentCommit.commitMessage)
        val diffSummary = require(parentCommit.diffSummary)

        val fileOperation = require(listenResource.fileOperation)
        val fileOperationTargetFilePath = require(targetFile.filePath)
        val fileOperationDescription = require(listenResource.description)

        val fileOperationKind = fileOperation.value?.let { FileOperation.parseLine(it) }

        val targetFileSegments: List<ArbrFileSegment> =
            if (fileOperationKind == FileOperation.EDIT_FILE || fileOperationKind == FileOperation.DELETE_FILE) {
                requireThatValue(targetFile.fileSegments) {
                    it.isNotEmpty()
                }.map { it.value }
            } else {
                // Irrelevant, so don't block on file segments, for example for new files not yet written
                emptyList()
            }

        val firstSegmentContentType: ArbrFileSegmentContentTypeValue?
        val firstSegmentRuleName: ArbrFileSegmentRuleNameValue?
        if (fileOperationKind == FileOperation.EDIT_FILE || fileOperationKind == FileOperation.DELETE_FILE) {
            firstSegmentContentType = require(targetFileSegments.first().contentType)
            firstSegmentRuleName = require(targetFileSegments.first().ruleName)
        } else {
            firstSegmentContentType = null
            firstSegmentRuleName = null
        }

        val fileSegmentContainers = if (fileOperationKind == FileOperation.EDIT_FILE) {
            val targetFileContent = require(targetFile.content)
            targetFileSegments.map { fileSegment ->
                val simpleRuleName = require(fileSegment.ruleName)
                val identifierName = require(fileSegment.name)
                val elementIndex = require(fileSegment.elementIndex)

                val startIndex = require(fileSegment.startIndex).value.toInt()
                val endIndex = require(fileSegment.endIndex).value.toInt()
                val fileSegmentContent = try {
                    ArbrFileSegmentOp.Content.initialize(
                        targetFileContent.kind,
                        (targetFileContent.value ?: "").substring(startIndex, endIndex),
                        targetFileContent.generatorInfo,
                    )
                } catch (e: StringIndexOutOfBoundsException) {
                    logger.warn("[$startIndex, $endIndex] (${targetFileContent.value?.length})[${targetFileContent.value}]")
                    throw RequiredValueMissingException(String::class.java)
                }

                SourcedStruct4(
                    ArbrFileSegmentOp.RuleName.initialize(
                        simpleRuleName.kind,
                        simpleRuleName.value,
                        simpleRuleName.generatorInfo,
                    ),
                    ArbrFileSegmentOp.Name.initialize(
                        identifierName.kind,
                        identifierName.value,
                        identifierName.generatorInfo,
                    ),
                    ArbrFileSegmentOp.ElementIndex.initialize(
                        elementIndex.kind,
                        elementIndex.value,
                        elementIndex.generatorInfo,
                    ),
                    fileSegmentContent,
                )
            }
        } else {
            emptyList()
        }

        return { partialObjectGraph ->
            val writeFileOp = partialObjectGraph.root

            val fileSegOpsMono = when (fileOperationKind) {
                FileOperation.CREATE_FILE -> {
                    // Create
                    fileOpNewToFileSegOps.invoke(
                        projectFullName,
                        taskQuery,
                        subtask,
                        commitMessage,
                        diffSummary,
                        fileOperation,
                        fileOperationTargetFilePath,
                        fileOperationDescription,
                        artifactSink.adapt(),
                    ).flatMap { (fileSegOps) ->
                        Flux.fromIterable(fileSegOps.containers)
                            .map { (opValue, contentType, ruleName, fileSegName, elementIndex, description) ->
                                detachedFileSegmentOp(
                                    partialObjectGraph,
                                    writeFileOp,
                                    opValue,
                                    contentType,
                                    ruleName,
                                    fileSegName,
                                    elementIndex,
                                    description,
                                )
                            }
                            .collectList()
                    }
                        .single()
                }

                FileOperation.EDIT_FILE -> {
                    // Edit
                    fileOpEditToFileSegOps.invoke(
                        projectFullName,
                        taskQuery,
                        subtask,
                        commitMessage,
                        fileOperation,
                        fileOperationTargetFilePath,
                        fileOperationDescription,
                        FileSegments.initializeMerged(
                            fileSegmentContainers
                        ),
                        artifactSink.adapt(),
                    ).flatMap { (fileSegOps) ->
                        Flux.fromIterable(fileSegOps.containers)
                            .map { (opValue, contentType, ruleName, fileSegName, elementIndex, description) ->
                                detachedFileSegmentOp(
                                    partialObjectGraph,
                                    writeFileOp,
                                    opValue,
                                    contentType,
                                    ruleName,
                                    fileSegName,
                                    elementIndex,
                                    description,
                                )
                            }
                            .collectList()
                    }
                        .single()
                }

                FileOperation.DELETE_FILE -> {
                    val fileSegOperationValue = "delete"

                    // Delete
                    Mono.just(
                        listOf(
                            detachedFileSegmentOp(
                                partialObjectGraph,
                                writeFileOp,
                                ArbrFileSegmentOp.Operation.initialize(
                                    fileOperation.kind,
                                    fileSegOperationValue,
                                    fileOperation.generatorInfo,
                                ),
                                ArbrFileSegmentOp.ContentType.initialize(
                                    firstSegmentContentType!!.kind,
                                    firstSegmentContentType.value,
                                    firstSegmentContentType.generatorInfo,
                                ),
                                ArbrFileSegmentOp.RuleName.initialize(
                                    firstSegmentRuleName!!.kind,
                                    firstSegmentRuleName.value,
                                    firstSegmentRuleName.generatorInfo,
                                ),
                                ArbrFileSegmentOp.Name.initialize(
                                    firstSegmentRuleName.kind,
                                    null,
                                    firstSegmentRuleName.generatorInfo,
                                ),
                                ArbrFileSegmentOp.ElementIndex.initialize(
                                    firstSegmentRuleName.kind,
                                    0,
                                    firstSegmentRuleName.generatorInfo,
                                ),
                                ArbrFileSegmentOp.Description.initialize(
                                    fileOperationDescription.kind,
                                    fileOperationDescription.value,
                                    fileOperationDescription.generatorInfo,
                                ),
                            )
                        )
                    )
                }

                null -> {
                    logger.error("Invalid file operation string ${fileOperation.value}")
                    Mono.just(emptyList())
                }
            }

            fileSegOpsMono.map { fileSegOps ->
                logger.info("Adding file seg ops to ${parentCommit.uuid}")

                writeFileOp.fileSegmentOps = immutableLinkedMapOfPartials(fileSegOps)
            }.then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileOp,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrFileOp,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val parentCommit = requireLatest(listenResource.parent).resource()
            ?: return
        val subtaskResource = requireLatest(parentCommit.parent).resource()
            ?: return
        requireLatest(listenResource.targetFile).resource()
            ?: return
        requireLatest(subtaskResource.parent).resource()
            ?: return

        val fileSegmentOps = requireLatestChildren(listenResource.fileSegmentOps.items)
        fileSegmentOps.forEach { (_, fileSegmentOp) ->
            requireLatest(fileSegmentOp.operation)
            requireLatest(fileSegmentOp.ruleName)
            requireLatest(fileSegmentOp.name)
            requireLatest(fileSegmentOp.elementIndex)
            requireLatest(fileSegmentOp.description)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileOpToFileSegOpsProcessor::class.java)
    }
}