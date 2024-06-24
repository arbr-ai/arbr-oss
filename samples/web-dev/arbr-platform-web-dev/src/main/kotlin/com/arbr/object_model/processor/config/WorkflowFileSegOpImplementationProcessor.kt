package com.arbr.object_model.processor.config

import com.arbr.core_web_dev.util.file_segments.FileSegmentOperationUtils
import com.arbr.core_web_dev.util.file_segments.morphism.FileSegmentTreeMapper
import com.arbr.core_web_dev.util.file_segments.morphism.SegmentEditGraphMorphism
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.util.adapt
import com.arbr.object_model.core.partial.*
import com.arbr.object_model.core.resource.*
import com.arbr.object_model.core.resource.field.*
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.internal.tree_parse.TreeParseFunctions
import com.arbr.og.object_model.impl.*
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.prompt_library.util.FileSegmentOperationsInFileWithContent
import com.arbr.prompt_library.util.FileSegmentOperationsInFileWithContentContainer
import com.arbr.relational_prompting.generics.EmbeddingProvider
import com.arbr.relational_prompting.generics.model.OpenAiEmbeddingRequest
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.reactor.single
import com.arbr.util_common.typing.cls
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import kotlin.math.pow

/**
 * Implements a file segment op.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-seg-op-impl", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileSegOpImplementationProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
    private val treeParseFunctions: TreeParseFunctions,
    private val embeddingProvider: EmbeddingProvider,
    private val mapper: ObjectMapper,
    @Qualifier("yamlMapper")
    private val yamlMapper: ObjectMapper,
) : ArbrResourceFunction<
        ArbrFileSegmentOp, PartialFileSegmentOp,
        ArbrProject, PartialProject,
        ArbrFile, PartialFile,
        >(objectModelParser) {
    private val newFileOpSegApplication = promptLibrary.newFileOpSegApplication
    private val editExistingFileOpSegApplication = promptLibrary.editExistingFileOpSegApplication
    private val newSegmentIncorporateApplication = promptLibrary.newSegmentIncorporateApplication

    override val name: String
        get() = "file-seg-op-impl"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrFile>
        get() = cls()

    @Suppress("DEPRECATION")
    private val writer = mapper
        .copy()
        .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        .writerWithDefaultPrettyPrinter()

    private fun matchFileSegment(
        fileContent: ArbrFileContentValue,
        /**
         * Actual file segments in the target file, to match against.
         */
        targetFileSegments: List<PartialFileSegment>,
        contentType: ArbrFileSegmentOpContentTypeValue,
        ruleName: ArbrFileSegmentOpRuleNameValue,
        fileSegName: ArbrFileSegmentOpNameValue,
        elementIndex: ArbrFileSegmentOpElementIndexValue,
        description: ArbrFileSegmentOpDescriptionValue,
    ): Mono<PartialFileSegment> {
        if (targetFileSegments.isEmpty()) {
            return Mono.empty()
        }

        val fileContentValue = fileContent.value
        val targetSegmentMapStrings = targetFileSegments.mapNotNull {
            val startIndex = it.startIndex?.value?.toInt()
            val endIndex = it.endIndex?.value?.toInt()

            if (fileContentValue == null || startIndex == null || endIndex == null) {
                null
            } else {
                val content = fileContentValue.substring(startIndex, endIndex)

                writer.writeValueAsString(
                    linkedMapOf(
                        "contentType" to it.contentType?.value,
                        "ruleName" to it.ruleName?.value,
                        "name" to it.name?.value,
                        "elementIndex" to it.elementIndex?.value,
                        "content" to content,
                    )
                )
            }
        }

        val thisFileSegOpString = writer.writeValueAsString(
            linkedMapOf(
                "contentType" to contentType.value,
                "ruleName" to ruleName.value,
                "name" to fileSegName.value,
                "elementIndex" to elementIndex.value,
                "description" to description.value,
            )
        )

        // TODO: Handle case when file content is invalid and therefore not segmented correctly
        return embeddingProvider.getEmbedding(
            OpenAiEmbeddingRequest(
                "text-embedding-ada-002",
                targetSegmentMapStrings + thisFileSegOpString,
                null,
            )
        ).map { resp ->
            val targetSegmentEmbeddings = resp.data.dropLast(1).map { it.embedding }
            val thisFileSegOp = resp.data.last().embedding

            val closestIdx = targetSegmentEmbeddings.withIndex().minBy { (_, vec) ->
                thisFileSegOp
                    .zip(vec) { x0, x1 -> (x0 - x1).pow(2) }
                    .sum()
            }.index

            logger.info("Closest file seg:\nInferred: ${thisFileSegOpString}\nActual:${targetSegmentMapStrings[closestIdx]}")

            targetFileSegments[closestIdx]
        }
    }

    private fun injectNewFileSegment(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        currentFileSegmentOp: PartialFileSegmentOp,
        targetFile: PartialFile,
        newFileSegmentContentType: ArbrFileSegmentOpContentTypeValue,
        detachedFileSegmentRuleName: ArbrFileSegmentOpRuleNameValue,
        detachedFileSegmentName: ArbrFileSegmentOpNameValue,
        detachedFileSegmentElementIndex: ArbrFileSegmentOpElementIndexValue,
        detachedFileSegmentContent: ArbrFileSegmentOpContentValue,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Void> {
        val fileSegmentTreeMapper = FileSegmentTreeMapper.PlaceNewSegment(
            newSegmentIncorporateApplication,
            yamlMapper,
            artifactSink,
            targetFile,
            detachedFileSegmentRuleName,
            detachedFileSegmentName,
            detachedFileSegmentElementIndex,
        )

        val incorporateNewSegmentMorphism = SegmentEditGraphMorphism(
            partialObjectGraph,
            fileSegmentTreeMapper,
            treeParseFunctions,
            currentFileSegmentOp,
            targetFile,
            newFileSegmentContentType,
            detachedFileSegmentRuleName,
            detachedFileSegmentName,
            detachedFileSegmentContent,
            reSegmentAfterIncorporation = true,
        )

        return incorporateNewSegmentMorphism.updateFileSegments()
            .map {
                logger.info(yamlMapper.writeValueAsString(targetFile))
                logger.info(yamlMapper.writeValueAsString(it))
                it
            }
    }

    private fun implementNewSegment(
        targetFile: ArbrFile,
        dependencyContainers: List<FileSegmentOperationsInFileWithContentContainer>,
        currentFileSegmentOp: ArbrFileSegmentOp,
        opValue: ArbrFileSegmentOpOperationValue,
        contentType: ArbrFileSegmentOpContentTypeValue,
        ruleName: ArbrFileSegmentOpRuleNameValue,
        name: ArbrFileSegmentOpNameValue,
        elementIndex: ArbrFileSegmentOpElementIndexValue,
        description: ArbrFileSegmentOpDescriptionValue,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<*, *, ArbrForeignKey>) -> Mono<Void> {
        return { partialObjectGraph ->
            newFileOpSegApplication.invoke(
                FileSegmentOperationsInFileWithContent.initializeMerged(dependencyContainers),
                opValue,
                contentType,
                ruleName,
                name,
                elementIndex,
                description,
                artifactSink.adapt(),
            ).flatMap { (newFileSegmentContent) ->
                logger.info("File seg op impl proposing new content on segment for op ${currentFileSegmentOp.uuid}")
                injectNewFileSegment(
                    partialObjectGraph,
                    partialObjectGraph.get<PartialFileSegmentOp>(currentFileSegmentOp.uuid)!!,
                    partialObjectGraph.get<PartialFile>(targetFile.uuid)!!,
                    contentType,
                    ruleName,
                    name,
                    elementIndex,
                    newFileSegmentContent,
                    artifactSink,
                )
                    .doOnTerminate {
                        logger.info("New segment add in ${targetFile.filePath.getLatestAcceptedValue()?.value} brought content from ${targetFile.content.getLatestAcceptedValue()?.value?.length} bytes to ${targetFile.content.getLatestAcceptedValue()?.value?.length} bytes")
                    }
            }
        }
    }

    private fun editExistingSegment(
        targetFile: ArbrFile,
        dependencyContainers: List<FileSegmentOperationsInFileWithContentContainer>,
        currentFileSegmentOp: ArbrFileSegmentOp,
        opValue: ArbrFileSegmentOpOperationValue,
        contentType: ArbrFileSegmentOpContentTypeValue,
        ruleName: ArbrFileSegmentOpRuleNameValue,
        name: ArbrFileSegmentOpNameValue,
        elementIndex: ArbrFileSegmentOpElementIndexValue,
        description: ArbrFileSegmentOpDescriptionValue,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<*, *, ArbrForeignKey>) -> Mono<Void> {
        require(targetFile.content)

        require(targetFile.fileSegments).forEach { (_, fileSeg) ->
            require(fileSeg.startIndex)
            require(fileSeg.endIndex)
        }

        return { partialObjectGraph ->
            val writeFileSegmentOp = partialObjectGraph.get<PartialFileSegmentOp>(currentFileSegmentOp.uuid)!!
            val writeFile = partialObjectGraph.get<PartialFile>(targetFile.uuid)!!

            val targetFileContent = writeFile.content!!
            val targetFileSegments = writeFile.fileSegments!!

            val latestTargetFileSegmentContents = targetFileSegments
                .mapNotNull { (_, segment) ->
                    val startIndex = segment.startIndex
                    val endIndex = segment.endIndex

                    if (startIndex == null || endIndex == null) {
                        null
                    } else {
                        segment.uuid to ArbrFileSegmentOp.Content.initialize(
                            targetFileContent.kind,
                            (targetFileContent.value ?: "").substring(
                                startIndex.value.toInt(),
                                endIndex.value.toInt(),
                            ),
                            targetFileContent.generatorInfo,
                        )
                    }
                }.toMap()

            matchFileSegment(
                targetFileContent,
                targetFileSegments.values.toList(),
                contentType,
                ruleName,
                name,
                elementIndex,
                description,
            )
                .single("No matching segment to edit in file for ${writeFileSegmentOp.uuid}")
                .materialize()
                .flatMap { fileSegmentSignal ->
                    val fileSegment = fileSegmentSignal.get()!!
                    val segmentContent = latestTargetFileSegmentContents[fileSegment.uuid]!!

                    editExistingFileOpSegApplication.invoke(
                        FileSegmentOperationsInFileWithContent.initializeMerged(dependencyContainers),
                        opValue,
                        contentType,
                        ruleName,
                        name,
                        elementIndex,
                        segmentContent,
                        description,
                        artifactSink.adapt(),
                    )
                        .flatMap { (newFileSegmentContent) ->
                            // Add the real, attached file segment
                            logger.info("File seg op impl proposing content edit on segment for op ${writeFileSegmentOp.uuid}")

                            val fileSegmentTreeMapper = FileSegmentTreeMapper.EditExistingSegment(
                                fileSegment
                            )
                            val incorporateNewSegmentMorphism = SegmentEditGraphMorphism(
                                partialObjectGraph,
                                fileSegmentTreeMapper,
                                treeParseFunctions,
                                writeFileSegmentOp,
                                writeFile,
                                writeFileSegmentOp.contentType!!,
                                writeFileSegmentOp.ruleName!!,
                                writeFileSegmentOp.name!!,
                                newFileSegmentContent,
                                reSegmentAfterIncorporation = true,
                            )

                            incorporateNewSegmentMorphism.updateFileSegments()
                                .doOnTerminate {
                                    logger.info("Segment edit in ${writeFile.filePath?.value} brought content from ${writeFile.content?.value?.length} bytes to ${writeFile.content?.value?.length} bytes")
                                }
                        }
                }
        }
    }

    private fun deleteSegment(
        targetFile: ArbrFile,
        currentFileSegmentOp: ArbrFileSegmentOp,
        contentType: ArbrFileSegmentOpContentTypeValue,
        ruleName: ArbrFileSegmentOpRuleNameValue,
        fileSegName: ArbrFileSegmentOpNameValue,
        elementIndex: ArbrFileSegmentOpElementIndexValue,
        description: ArbrFileSegmentOpDescriptionValue,
    ): (PartialObjectGraph<*, *, ArbrForeignKey>) -> Mono<Void> {
        return { partialObjectGraph ->
            val writeFileSegmentOp = partialObjectGraph.get<PartialFileSegmentOp>(currentFileSegmentOp.uuid)!!
            val writeFile = partialObjectGraph.get<PartialFile>(targetFile.uuid)!!

            val targetFileContent = writeFile.content

            if (targetFileContent == null) {
                Mono.empty()
            } else {
                val targetFileSegments = (writeFile.fileSegments ?: ImmutableLinkedMap())

                matchFileSegment(
                    targetFileContent,
                    targetFileSegments.values.toList(),
                    contentType,
                    ruleName,
                    fileSegName,
                    elementIndex,
                    description,
                )
                    .single("No matching segment to delete in file for ${writeFileSegmentOp.uuid}")
                    .flatMap { fileSegment ->
                        // Add the real (deleted) file segment
                        logger.info("File seg op impl proposing content delete on segment for op ${writeFileSegmentOp.uuid}")

                        val matchingSegmentRuleName = ArbrFileSegmentOp.RuleName.initialize(
                            fileSegment.ruleName!!.kind,
                            fileSegment.ruleName!!.value,
                            fileSegment.ruleName!!.generatorInfo,
                        )
                        val matchingSegmentName = ArbrFileSegmentOp.Name.initialize(
                            fileSegment.name!!.kind,
                            fileSegment.name!!.value,
                            fileSegment.name!!.generatorInfo,
                        )
                        val fileSegmentTreeMapper = FileSegmentTreeMapper.DeleteSegment(
                            fileSegment,
                        )
                        val incorporateNewSegmentMorphism = SegmentEditGraphMorphism(
                            partialObjectGraph,
                            fileSegmentTreeMapper,
                            treeParseFunctions,
                            writeFileSegmentOp,
                            writeFile,
                            writeFileSegmentOp.contentType!!,
                            matchingSegmentRuleName,
                            matchingSegmentName,
                            ArbrFileSegmentOp.Content.constant(""),
                            reSegmentAfterIncorporation = true,
                        )

                        incorporateNewSegmentMorphism.updateFileSegments()
                    }
            }
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrFileSegmentOp,
        readResource: ArbrProject,
        writeResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrFile, PartialFile, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.implementedFileSegment.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val contentType = require(listenResource.contentType)
        val ruleName = require(listenResource.ruleName)
        val name = require(listenResource.name)
        val elementIndex = require(listenResource.elementIndex)
        val description = require(listenResource.description)
        val operation = require(listenResource.operation)

        val dependencyList = require(listenResource.parentOfFileSegmentOpDependency)
            .map {
                if (it.value.dependencyFileSegmentOp.getLatestAcceptedValue()?.resource() == null) {
                    logger.error("Missing dependency!")
                }

                requireAttachedOrElseComplete(it.value.dependencyFileSegmentOp)
            }

        val dependencyImplementations = dependencyList.mapNotNull { dependencyFileSegmentOp ->
            // Valid and expected for `implementedFileSegment` to be null initially
            // We require that the implementation be set, but if it's set to null, we won't block on the existence of an
            // implementation
            require(dependencyFileSegmentOp.implementedFileSegment).resource()?.let {
                dependencyFileSegmentOp to it
            }
        }

        // Check that each dependency FileSegmentOp is implemented, i.e.:
        // What happens if an implementation is later invalidated?
        // TODO: Include a few generations up to some limit
        val dependencyContainers =
            dependencyImplementations.mapNotNull { (dependencyFileSegmentOp, dependencyImplementedFileSegment) ->
                Invariants.check { req ->
                    req(dependencyImplementedFileSegment.parent.getLatestAcceptedValue() != null)
                }
                val fileReference = require(dependencyImplementedFileSegment.parent)

                // TODO:
                // Situation: the segment used to be attached to the file, but then the file got re-segmented and now
                // it's detached
                // One idea: use the arbiter to retain implemented operations
                // For now, just don't include these as examples, since they are technically outdated anyway
                if (fileReference.resource() == null) {
                    logger.warn("Dependency of file segment op missing implemented file; likely detached:\nCurrent op: ${listenResource.uuid}\nDependency: ${dependencyFileSegmentOp.uuid}\nSegment: ${dependencyImplementedFileSegment.uuid}")
                    null
                } else {
                    val file = requireAttachedOrElseComplete(dependencyImplementedFileSegment.parent)

                    val attachedFileContent = require(file.content)
                    val startIndex = require(dependencyImplementedFileSegment.startIndex)
                    val endIndex = require(dependencyImplementedFileSegment.endIndex)
                    val attachedFileContentValue = attachedFileContent.value ?: ""

                    Invariants.check { require ->
                        val validStartIndex =
                            startIndex.value >= 0 && startIndex.value <= attachedFileContentValue.length
                        val validEndIndex =
                            endIndex.value >= 0 && endIndex.value <= attachedFileContentValue.length

                        if (!validStartIndex || !validEndIndex) {
                            logger.error("Start index invalid: (${startIndex.value}, ${endIndex.value}) for content of length ${attachedFileContentValue.length}")
                            logger.error("On file: ${file.filePath.getLatestAcceptedValue()?.value}")
                            logger.error("Content:\n${attachedFileContent.value}")
                        }

                        require(validStartIndex)
                        require(validEndIndex)
                    }

                    val fileSegmentContentValue = attachedFileContentValue.substring(
                        startIndex.value.toInt(),
                        endIndex.value.toInt(),
                    )
                    val fileSegmentContent = ArbrFileSegmentOp.Content.initialize(
                        startIndex.kind,
                        fileSegmentContentValue,
                        startIndex.generatorInfo,
                    )

                    FileSegmentOperationsInFileWithContentContainer(
                        require(file.filePath),
                        require(dependencyFileSegmentOp.operation),
                        require(dependencyFileSegmentOp.contentType),
                        require(dependencyFileSegmentOp.ruleName),
                        require(dependencyFileSegmentOp.name),
                        require(dependencyFileSegmentOp.elementIndex),
                        fileSegmentContent,
                        require(dependencyFileSegmentOp.description),
                    )
                }
            }

        val updateMono = when (val operationValue = operation.value) {
            "add" -> {
                logger.info("Implementing new file segment [${ruleName.value} ${name.value}] in ${writeResource.filePath.getLatestAcceptedValue()?.value}")
                implementNewSegment(
                    writeResource,
                    dependencyContainers,
                    listenResource,
                    operation,
                    contentType,
                    ruleName,
                    name,
                    elementIndex,
                    description,
                    artifactSink,
                )
            }

            "edit" -> {
                logger.info("Editing existing file segment [${ruleName.value} ${name.value}] in ${writeResource.filePath.getLatestAcceptedValue()?.value}")
                editExistingSegment(
                    writeResource,
                    dependencyContainers,
                    listenResource,
                    operation,
                    contentType,
                    ruleName,
                    name,
                    elementIndex,
                    description,
                    artifactSink,
                )
            }

            "delete" -> {
                logger.info("Deleting file segment [${ruleName.value} ${name.value}] in ${writeResource.filePath.getLatestAcceptedValue()?.value}")
                deleteSegment(
                    writeResource,
                    listenResource,
                    contentType,
                    ruleName,
                    name,
                    elementIndex,
                    description,
                )
            }

            else -> {
                return { partialObjectGraph ->
                    val writeFileSegmentOp = partialObjectGraph.get<PartialFileSegmentOp>(listenResource.uuid)!!
                    writeFileSegmentOp.implementedFileSegment = PartialRef(null)
                    Mono.empty()
                }
            }
        }

        return { partialObjectGraph ->
            updateMono(partialObjectGraph).then(
                Mono.defer {
                    val writeFile = partialObjectGraph.root
                    val fileSegmentItems = writeFile.fileSegments?.values?.toList()

                    if (fileSegmentItems != null) {
                        val oldSegDump = fileSegmentItems.joinToString("\n") {
                            listOf(
                                it.parentSegment?.uuid,
                                it.uuid,
                                it.startIndex?.value,
                                it.endIndex?.value,
                                it.ruleName?.value,
                                it.name?.value
                            ).joinToString(", ") { s -> s?.toString() ?: "" }
                        }
                        val segDump = fileSegmentItems.joinToString("\n") {
                            listOf(
                                it.parentSegment?.uuid,
                                it.uuid,
                                it.startIndex?.value,
                                it.endIndex?.value,
                                it.ruleName?.value,
                                it.name?.value
                            ).joinToString(", ") { s -> s?.toString() ?: "" }
                        }
                        logger.info("Got new segments for ${operation.value} on ${writeFile.filePath?.value}:\nOld Segments:\n$oldSegDump\n\nNew Segments:\n$segDump\n")
                    }

                    Invariants.check { require ->
                        require(fileSegmentItems != null)
                        require(writeFile.content != null)
                        require(
                            try {
                                FileSegmentOperationUtils.coversContent(
                                    writeFile.filePath!!,
                                    writeFile.content!!,
                                    fileSegmentItems!!,
                                ) { message ->
                                    val oldSegDump = fileSegmentItems.toList().joinToString("\n") {
                                        listOf(
                                            it.parentSegment?.uuid,
                                            it.uuid,
                                            it.startIndex?.value,
                                            it.endIndex?.value,
                                            it.ruleName?.value,
                                            it.name?.value
                                        ).joinToString(", ") { s -> s?.toString() ?: "" }
                                    }
                                    val segDump = fileSegmentItems.joinToString("\n") {
                                        listOf(
                                            it.parentSegment?.uuid,
                                            it.uuid,
                                            it.startIndex?.value,
                                            it.endIndex?.value,
                                            it.ruleName?.value,
                                            it.name?.value
                                        ).joinToString(", ") { s -> s?.toString() ?: "" }
                                    }
                                    logger.error("File Seg Op Impl invariant failed: $message\n\nOld Segments:\n$oldSegDump\n\nNew Segments:\n$segDump")

                                    throw Exception()
                                }
                                true
                            } catch (e: Exception) {
                                false
                            }
                        )
                    }

                    Mono.empty()
                }
            )
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrFileSegmentOp,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        // Require implemented and attached
        val implementedFileSegmentRef = requireLatest(listenResource.implementedFileSegment)

        implementedFileSegmentRef.resource()?.let { implementedFileSegment ->
            requireLatest(implementedFileSegment.parent)
            requireLatest(implementedFileSegment.startIndex)
            requireLatest(implementedFileSegment.endIndex)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileSegOpImplementationProcessor::class.java)
    }
}