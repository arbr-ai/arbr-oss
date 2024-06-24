package com.arbr.core_web_dev.util.file_segments.morphism

import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegmentOp
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.field.*
import com.arbr.core_web_dev.util.file_segments.*
import com.arbr.data_structures_common.immutable.immutableLinkedMapOf
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentContentType
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.util_common.invariants.Invariants
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.graph.AlignableHomogenousRootedListDAG
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAGAlignmentOperation
import com.arbr.alignable.morphism.DifferentialListGraphMorphism
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*
import kotlin.jvm.optionals.getOrNull
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.internal.tree_parse.TreeParseFunctions

class ReSegmentGraphMorphism(
    private val treeParseFunctions: TreeParseFunctions,
    private val partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
    private val currentFileSegmentOp: PartialFileSegmentOp,
    private val targetFile: PartialFile,
    private val fileSegmentContentType: ArbrFileSegmentOpContentTypeValue,
) : DifferentialListGraphMorphism<FileSegmentTree, FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>() {
    override fun encodeElement(sourceNodeValue: FileSegmentNode): AlignableSegmentIndexReprTreeNode {
        val nodeProperties = sourceNodeValue.properties
        return AlignableSegmentIndexReprTreeNode(
            nodeProperties.contentType?.value?.let { stringContentTypeValue ->
                SegmentContentType.values().firstOrNull {
                    it.serializedName.lowercase() == stringContentTypeValue.lowercase()
                } ?: SegmentContentType.Plaintext
            },
            nodeProperties.ruleName?.value,
            nodeProperties.name?.value?.let { Optional.of(it) },
            nodeProperties.elementIndex?.value?.toInt(),
            nodeProperties.startIndex?.value?.toInt(),
            nodeProperties.endIndex?.value?.toInt(),
        )
    }

    override fun addNewChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>>
    ): Mono<Void> {
        logger.info("Re-segmenter: Add child type $childKey on ${parentGraph.nodeValue.sourceValue?.properties?.uuid}")
        return Mono.empty()
    }

    override fun applySourceNodeOperation(
        sourceNodeValue: FileSegmentNode?,
        operation: AlignableSegmentIndexReprTreeNode
    ): Mono<Void> {
        logger.info("Re-segmenter: Apply op on ${sourceNodeValue?.properties?.uuid}:\n${operation}")

        return Mono.empty()
    }

    override fun attachNewSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>,
        childKey: String,
        atIndex: Int,
        subgraphToAttach: AlignableHomogenousRootedListDAG<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>
    ): Mono<Void> {
        logger.info("Re-segmenter: Attach child at $childKey $atIndex to ${parentGraph.nodeValue.sourceValue?.properties?.uuid}\n${subgraphToAttach}")

        return Mono.empty()
    }

    override fun initializeSourceValue(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>,
        newSubgraph: AlignableHomogenousRootedListDAG<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>
    ): FileSegmentNode? {
        return null
    }

    override fun removeChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>>
    ): Mono<Void> {
        logger.info("Re-segmenter: Remove child type $childKey on ${parentGraph.nodeValue.sourceValue?.properties?.uuid}")
        return Mono.empty()
    }

    override fun removeSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>, AlignableSegmentIndexReprTreeNode>,
        childKey: String,
        atIndex: Int,
        subgraphToRemove: AlignableHomogenousRootedListDAG<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>
    ): Mono<Void> {
        logger.info("Re-segmenter: Remove child at $childKey $atIndex to ${parentGraph.nodeValue.sourceValue?.properties?.uuid}\n${subgraphToRemove}")
        return Mono.empty()
    }

    override fun applyInnerMap(sourceGraph: AlignableHomogenousRootedListDAG<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>): Mono<AlignableHomogenousRootedListDAG<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode>> {
        val targetFileSegments = treeParseFunctions.computeSegments(
            partialObjectGraph,
            targetFile.uuid,
            targetFile.filePath!!,
            targetFile.content!!,
        )
        val rootFileSegments = targetFileSegments.filter { it.parentSegment == null }
        val rootFileSegment = if (rootFileSegments.isEmpty()) {
            return Mono.error(Exception("No root segment on initialized file"))
        } else if (rootFileSegments.size > 1) {
            return Mono.error(Exception("Multiple root file segments on initialized file"))
        } else {
            rootFileSegments.first()
        }

        val dag = HomogenousRootedListDAG.fromGenerator(
            rootFileSegment,
            { it },
            {
                it.fileSegments?.values?.toList()?.let { segs ->
                    mapOf(fileSegmentsKey to segs)
                } ?: emptyMap()
            }
        ).flatMapAlignable {
            AlignableSegmentIndexReprTreeNode(
                it.contentType?.value?.let { stringContentTypeValue ->
                    SegmentContentType.values().firstOrNull { segmentContentType ->
                        segmentContentType.serializedName.lowercase() == stringContentTypeValue.lowercase()
                    } ?: SegmentContentType.Plaintext
                },
                it.ruleName?.value,
                it.name?.value?.let { v -> Optional.of(v) },
                it.elementIndex?.value?.toInt(),
                it.startIndex?.value?.toInt(),
                it.endIndex?.value?.toInt(),
            )
        }

        return Mono.just(dag)
    }

    private fun makeNewSegmentNode(
        parentUuid: String?,
        newChildSegmentInfo: AlignableSegmentIndexReprTreeNode,
    ): FileSegmentNode {
        val uuid = UUID.randomUUID().toString()

        val ruleName = newChildSegmentInfo.ruleName
        Invariants.check { require ->
            require(ruleName != null)
        }

        val elementIndex = newChildSegmentInfo.elementIndex?.toLong()
        Invariants.check { require ->
            require(elementIndex != null)
        }

        val startIndex = newChildSegmentInfo.startIndex?.toLong()
        Invariants.check { require ->
            require(startIndex != null)
        }

        val endIndex = newChildSegmentInfo.endIndex?.toLong()
        Invariants.check { require ->
            require(endIndex != null)
        }

        return FileSegmentNode(
            FileSegmentProperties(
                uuid,
                contentType = ArbrFileSegment.ContentType.initialize(
                    fileSegmentContentType.kind,
                    fileSegmentContentType.value,
                    fileSegmentContentType.generatorInfo,
                ),
                ruleName = ArbrFileSegment.RuleName.initialize(
                    fileSegmentContentType.kind,
                    ruleName ?: "miscellaneous",
                    fileSegmentContentType.generatorInfo,
                ),
                name = ArbrFileSegment.Name.initialize(
                    fileSegmentContentType.kind,
                    newChildSegmentInfo.name?.getOrNull(),
                    fileSegmentContentType.generatorInfo,
                ),
                elementIndex = ArbrFileSegment.ElementIndex.initialize(
                    fileSegmentContentType.kind,
                    elementIndex ?: 0L,
                    fileSegmentContentType.generatorInfo,
                ),
                startIndex = ArbrFileSegment.StartIndex.initialize(
                    fileSegmentContentType.kind,
                    startIndex ?: 0L,
                    fileSegmentContentType.generatorInfo,
                ),
                endIndex = ArbrFileSegment.EndIndex.initialize(
                    fileSegmentContentType.kind,
                    endIndex ?: 0L,
                    fileSegmentContentType.generatorInfo,
                ),
                summary = null,
                containsTodo = null,
            ),
            FileSegmentParents(
                file = PartialRef(targetFile.uuid),
                parentSegment = parentUuid?.let { PartialRef(it) },
            ),
            FileSegmentHeterogenousChildren(
                fileSegmentOps = immutableLinkedMapOf(
                    currentFileSegmentOp.uuid to currentFileSegmentOp.apply {
                        implementedFileSegment = PartialRef(uuid)
                    }
                )
            ),
        )
    }

    fun updateFileSegments(): Mono<Void> {
        val targetFileSegmentTree = FileSegmentTree.ofFile(targetFile)
            ?: return Mono.error(Exception("Unable to construct tree on file segments of file ${targetFile.filePath}"))

        return apply(targetFileSegmentTree).flatMap { pairedResultGraph ->

            val newlyAddedSegments = mutableListOf<FileSegmentNode>()

            val unmappedGraph =
                pairedResultGraph.mapWithParent<FileSegmentNode> { proxyNode, parent ->
                    val existingSegment = proxyNode.sourceValue
                    if (existingSegment != null) {
                        Invariants.check { require ->
                            require(existingSegment.properties.startIndex != null)
                            require(existingSegment.properties.endIndex != null)
                        }

                        val fileSegmentTreeInfo = proxyNode.alignableElement
                        // Set properties here - this is janky
                        val ruleName = fileSegmentTreeInfo.ruleName
                        val updateRuleName =
                            if (ruleName != null && ruleName != existingSegment.properties.ruleName?.value) {
                                ArbrFileSegment.RuleName.computed(
                                    ruleName,
                                    SourcedValueGeneratorInfo(
                                        emptyList()
                                    )
                                )
                            } else {
                                null
                            }

                        val segmentName = fileSegmentTreeInfo.name?.getOrNull()
                        val updateSegmentName =
                            if (segmentName != null && segmentName != existingSegment.properties.name?.value) {
                                ArbrFileSegment.Name.computed(
                                    segmentName,
                                    SourcedValueGeneratorInfo(
                                        emptyList()
                                    )
                                )
                            } else {
                                null
                            }

                        existingSegment.copy(
                            properties = existingSegment.properties.copy(
                                contentType = fileSegmentTreeInfo.contentType?.let { newContentType ->
                                    existingSegment.properties.contentType?.map { newContentType.serializedName }
                                        ?: ArbrFileSegment.ContentType.constant(newContentType.serializedName)
                                } ?: existingSegment.properties.contentType,
                                ruleName = updateRuleName
                                    ?: existingSegment.properties.ruleName,
                                name = updateSegmentName ?: existingSegment.properties.name,
                                elementIndex = fileSegmentTreeInfo.elementIndex?.let { newElementIndex ->
                                    val newElementIndexLong = newElementIndex.toLong()
                                    existingSegment.properties.elementIndex?.map { newElementIndexLong }
                                        ?: ArbrFileSegment.ElementIndex.constant(newElementIndexLong)
                                } ?: existingSegment.properties.elementIndex,
                                startIndex = fileSegmentTreeInfo.startIndex?.let { newStartIndex ->
                                    val newStartIndexLong = newStartIndex.toLong()
                                    existingSegment.properties.startIndex?.map { newStartIndexLong }
                                        ?: ArbrFileSegment.StartIndex.constant(newStartIndexLong)
                                } ?: existingSegment.properties.startIndex,
                                endIndex = fileSegmentTreeInfo.endIndex?.let { newEndIndex ->
                                    val newEndIndexLong = newEndIndex.toLong()
                                    existingSegment.properties.endIndex?.map { newEndIndexLong }
                                        ?: ArbrFileSegment.EndIndex.constant(newEndIndexLong)
                                } ?: existingSegment.properties.endIndex,
                            )
                        )
                    } else {
                        logger.info("Re-segmenter: Creating new segment in unmapping at start index ${proxyNode.alignableElement.startIndex}: ${proxyNode.alignableElement.ruleName} ${proxyNode.alignableElement.name}")

                        makeNewSegmentNode(
                            parent?.properties?.uuid,
                            proxyNode.alignableElement,
                        ).also { newlyAddedSegments.add(it) }
                    }
                }
                    .let { FileSegmentTree(it.nodeValue, it.children) }

            val newFileSegmentPartials =
                immutableLinkedMapOfPartials(unmappedGraph.toPartials(partialObjectGraph))
            targetFile.fileSegments = newFileSegmentPartials

            Invariants.check { require ->
                require(FileSegmentTree.ofFile(targetFile) != null)
            }

            Mono.empty()
        }
    }

    companion object {
        private const val fileSegmentsKey = "DdlGithub.FileSegment.ParentSegment"

        private val logger = LoggerFactory.getLogger(ReSegmentGraphMorphism::class.java)
    }
}