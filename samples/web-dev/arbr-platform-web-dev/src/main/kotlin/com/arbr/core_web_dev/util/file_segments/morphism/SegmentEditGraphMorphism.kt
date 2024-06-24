package com.arbr.core_web_dev.util.file_segments.morphism

import com.arbr.core_web_dev.util.file_segments.*
import com.arbr.data_structures_common.immutable.immutableLinkedMapOf
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegmentOp
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.field.*
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.internal.tree_parse.TreeParseFunctions
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.util_common.invariants.Invariants
import com.arbr.util_common.reactor.nonBlocking
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.collections.AlignableList
import com.arbr.platform.alignable.alignable.graph.AlignableHomogenousRootedListDAG
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAGAlignmentOperation
import com.arbr.platform.alignable.alignable.struct.DoubleNullable
import com.arbr.alignable.morphism.DifferentialListGraphMorphism
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.*

class SegmentEditGraphMorphism(
    private val partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
    private val fileSegmentTreeMapper: FileSegmentTreeMapper,
    private val treeParseFunctions: TreeParseFunctions,
    private val currentFileSegmentOp: PartialFileSegmentOp,
    private val targetFile: PartialFile,
    private val newFileSegmentContentType: ArbrFileSegmentOpContentTypeValue,
    private val newFileSegmentRuleName: ArbrFileSegmentOpRuleNameValue,
    private val newFileSegmentName: ArbrFileSegmentOpNameValue,
    private val newFileSegmentContent: ArbrFileSegmentOpContentValue,
    private val reSegmentAfterIncorporation: Boolean,
) : DifferentialListGraphMorphism<FileSegmentTree, FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>() {
    override fun encodeElement(sourceNodeValue: FileSegmentNode): FileSegmentTreeInfo {
        val nodeProperties = sourceNodeValue.properties
        return FileSegmentTreeInfo(
            nodeProperties.ruleName,
            nodeProperties.name,
        )
    }

    override fun addNewChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>>
    ): Mono<Void> {
        logger.info("Add child type $childKey on ${parentGraph.nodeValue.sourceValue?.properties?.uuid}")
        return Mono.empty()
    }

    override fun applySourceNodeOperation(
        sourceNodeValue: FileSegmentNode?,
        operation: FileSegmentTreeInfoOperation
    ): Mono<Void> {
        val opString = StringBuilder().let { sb ->
            val t1 = operation.t1
            sb.append(
                when (t1) {
                    is DoubleNullable.Empty -> ""
                    is DoubleNullable.Null -> "ruleName=null\n"
                    is DoubleNullable.Some -> "ruleName=\"${t1.value.value}\"\n"
                }
            )
            val t2 = operation.t2
            sb.append(
                when (t2) {
                    is DoubleNullable.Empty -> ""
                    is DoubleNullable.Null -> "name=null\n"
                    is DoubleNullable.Some -> "name=\"${t2.value.value}\"\n"
                }
            )

            sb.toString()
        }

        logger.info("Apply op on ${sourceNodeValue?.properties?.uuid}:\n${opString}")
        return Mono.empty()
    }

    override fun attachNewSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>,
        childKey: String,
        atIndex: Int,
        subgraphToAttach: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>
    ): Mono<Void> {
        //
        logger.info("Attach child at $childKey $atIndex to ${parentGraph.nodeValue.sourceValue?.properties?.uuid}\n${subgraphToAttach}")

        return Mono.empty()
    }

    override fun initializeSourceValue(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>,
        newSubgraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>
    ): FileSegmentNode? {
        // This is more for listener use cases
        return null
    }

    override fun removeChildType(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>,
        childKey: String,
        childList: AlignableList<AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>, HomogenousRootedListDAGAlignmentOperation<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>>
    ): Mono<Void> {
        //
        logger.info("Remove child type $childKey on ${parentGraph.nodeValue.sourceValue?.properties?.uuid}")
        return Mono.empty()
    }

    override fun removeSourceElement(
        parentGraph: AlignableHomogenousRootedListDAG<AlignableProxy<FileSegmentNode, FileSegmentTreeInfo, FileSegmentTreeInfoOperation>, FileSegmentTreeInfoOperation>,
        childKey: String,
        atIndex: Int,
        subgraphToRemove: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>
    ): Mono<Void> {
        //
        logger.info("Remove child at $childKey $atIndex to ${parentGraph.nodeValue.sourceValue?.properties?.uuid}\n${subgraphToRemove}")
        return Mono.empty()
    }

    override fun applyInnerMap(sourceGraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>): Mono<AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>> {
        return fileSegmentTreeMapper.computeNewTree(sourceGraph)
    }

    private fun makeNewSegmentNode(
        parentUuid: String?,
        newChildSegmentInfo: FileSegmentTreeInfo,
        startIndex: ArbrFileSegmentStartIndexValue,
        endIndex: ArbrFileSegmentEndIndexValue,
    ): FileSegmentNode {
        val uuid = UUID.randomUUID().toString()

        return FileSegmentNode(
            FileSegmentProperties(
                uuid,
                contentType = ArbrFileSegment.ContentType.initialize(
                    newFileSegmentContentType.kind,
                    newFileSegmentContentType.value,
                    newFileSegmentContentType.generatorInfo,
                ),
                ruleName = newChildSegmentInfo.elementRuleName,
                name = newChildSegmentInfo.elementName,
                elementIndex = ArbrFileSegment.ElementIndex.constant(0L), // TODO
                startIndex = startIndex,
                endIndex = endIndex,
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

    // TODO: This would all be cleaner as a listener-based implementation
    fun updateFileSegments(): Mono<Void> {
        val initialFileSegments = targetFile.fileSegments ?: emptyMap()

        val targetFileSegmentTree = FileSegmentTree.ofFile(targetFile)
            ?: return Mono.error(Exception("Unable to construct tree on file segments of file ${targetFile.filePath}"))

        // Null is acceptable but content should be set
        val targetFileContent = targetFile.content
            ?: return Mono.error(Exception("Content not set on input file"))

        return apply(targetFileSegmentTree).flatMap { pairedResultGraph ->

            val newlyAddedSegments = mutableListOf<FileSegmentNode>()

            // TODO: Handle case of new generation more robustly - decide position relative to siblings
            val unmappedGraph =
                pairedResultGraph.mapWithParentAndPriorSibling<FileSegmentNode> { proxyNode, parent, priorSibling ->
                    val existingSegment = proxyNode.sourceValue
                    if (existingSegment != null) {
                        Invariants.check { require ->
                            require(existingSegment.properties.startIndex != null)
                            require(existingSegment.properties.endIndex != null)
                        }

                        existingSegment
                    } else {
                        val startIndexValue = priorSibling?.properties?.endIndex?.value
                            ?: parent?.properties?.startIndex?.value
                            ?: throw Exception("No reference start index")

                        logger.info("Creating new segment in unmapping at start index $startIndexValue: ${proxyNode.alignableElement.elementRuleName?.value} ${proxyNode.alignableElement.elementName?.value}")

                        // Add the segment as length-0 initially to not affect other segments
                        val endIndex = ArbrFileSegment.EndIndex.constant(startIndexValue)

                        makeNewSegmentNode(
                            parent?.properties?.uuid,
                            proxyNode.alignableElement,
                            ArbrFileSegment.StartIndex.constant(startIndexValue),
                            endIndex,
                        ).also { newlyAddedSegments.add(it) }
                    }
                }
                    .let { FileSegmentTree(it.nodeValue, it.children) }

            val unmappedSegments = unmappedGraph.flatten()

            Invariants.check { require ->
                if (fileSegmentTreeMapper is FileSegmentTreeMapper.PlaceNewSegment) {
                    require(unmappedSegments.isNotEmpty())

                    // Check that exactly one segment was added, but include an allowance for files with exactly 1
                    // segment, which might indicate the lack of a proper segmenter, in which case the create behaves
                    // like an edit to that segment.
                    if (unmappedSegments.size != 1) {
                        if (newlyAddedSegments.size != 1) {
                            logger.info(targetFileSegmentTree.toString())
                            logger.info(pairedResultGraph.toString())
                        }
                        require(newlyAddedSegments.size == 1)
                    }
                } else {
                    require(newlyAddedSegments.isEmpty())
                }
            }

            // TODO: Clean this up with sealed classes
            val segmentsForContentUpdate: List<Pair<String, ArbrFileSegmentOpContentValue>> =
                when (fileSegmentTreeMapper) {
                    is FileSegmentTreeMapper.DeleteSegment -> listOf(fileSegmentTreeMapper.targetFileSegment.uuid to newFileSegmentContent)
                    is FileSegmentTreeMapper.EditExistingSegment -> listOf(fileSegmentTreeMapper.targetFileSegment.uuid to newFileSegmentContent)
                    is FileSegmentTreeMapper.PlaceNewSegment -> {
                        val newSegmentTarget = if (newlyAddedSegments.size == 1) {
                            newlyAddedSegments.firstOrNull()
                        } else if (newlyAddedSegments.isEmpty() && unmappedSegments.size == 1) {
                            // Special case for content types without a proper segmenter
                            unmappedSegments.first()
                        } else {
                            // Multiple new segments
                            // Look for one with similar properties
                            newlyAddedSegments.firstOrNull {
                                (newFileSegmentName.value != null && it.properties.name?.value == newFileSegmentName.value)
                                        || (newFileSegmentName.value == null && it.properties.ruleName?.value == newFileSegmentRuleName.value)
                            }
                        }

                        listOfNotNull(newSegmentTarget).map { it.properties.uuid to newFileSegmentContent }
                    }
                }

            val (newFileSegments, newFileContent) = segmentsForContentUpdate.fold(unmappedGraph to targetFileContent) { (runningFileSegments, runningFileContent), (targetSegmentUuid, content) ->
                // Look up in running list to reflect updates
                val targetSegment = runningFileSegments.getNode(targetSegmentUuid)
                    ?: throw Exception("No node in tree matching $targetSegmentUuid")

                // Update the serial content relative to the old indices since the rectified indices will already
                // reflect the update
                val (nextFileContentValue, usedSegmentContentValue) = run {
                    val newSegmentContent = content.value

                    runningFileContent.value?.let { fileContentValue ->
                        val oldStartIndex = targetSegment.properties.startIndex!!.value.toInt()
                        val oldEndIndex = targetSegment.properties.endIndex!!.value.toInt()

                        FileSegmentOperationUtils.replaceRangeExclusive(
                            fileContentValue,
                            oldStartIndex,
                            oldEndIndex,
                            newSegmentContent ?: "",
                        )
                    } ?: (newSegmentContent to newSegmentContent)
                }

                val nextFileContent = runningFileContent.map {
                    nextFileContentValue
                }

                // Prune the parent node if the new content is null, i.e. this is a deletion.
                val pruneParent = content.value == null

                val nextSegments = FileSegmentOperationUtils.pruneFileSegmentDescendants(
                    runningFileSegments,
                    targetSegment,
                    false,
                ).let { prunedFs ->
                    FileSegmentOperationUtils.rectifyFileSegmentIndices(
                        prunedFs,
                        targetSegment,
                        content.map { usedSegmentContentValue },
                    )
                }.let { rectifiedFs ->
                    if (pruneParent) {
                        FileSegmentOperationUtils.pruneFileSegmentDescendants(
                            rectifiedFs,
                            targetSegment,
                            true,
                        )
                    } else {
                        rectifiedFs
                    }
                }

                Invariants.check { require ->
                    val nextSegmentPartials = nextSegments.toPartials(partialObjectGraph)
                    if (fileSegmentTreeMapper is FileSegmentTreeMapper.DeleteSegment) {
                        // Failing?
                        require(
                            nextSegmentPartials.none { it.uuid == targetSegment.properties.uuid }
                        )
                    } else {
                        val rectifiedNewFileSegment =
                            nextSegmentPartials.first { it.uuid == targetSegment.properties.uuid }
                        val newSegmentStartIndex = rectifiedNewFileSegment.startIndex!!.value.toInt()
                        val newSegmentEndIndex = rectifiedNewFileSegment.endIndex!!.value.toInt()

                        require(newSegmentEndIndex == newSegmentStartIndex + (usedSegmentContentValue?.length ?: 0))

                        if (nextFileContentValue != null && usedSegmentContentValue != null) {
                            try {
                                val actualSubstring = nextFileContentValue.substring(
                                    newSegmentStartIndex,
                                    newSegmentEndIndex
                                )
                                if (actualSubstring != usedSegmentContentValue) {
                                    logger.error("Segment edit content mismatch:\nExpected:\n[$usedSegmentContentValue]\nActual:[$actualSubstring]")
                                }

                                require(
                                    actualSubstring == usedSegmentContentValue
                                )
                            } catch (e: StringIndexOutOfBoundsException) {
                                require(newSegmentStartIndex >= 0)
                                require(newSegmentStartIndex < newSegmentEndIndex)

                                logger.error("Segment index violation; diff $newSegmentEndIndex - ${nextFileContentValue.length} = ${newSegmentEndIndex - nextFileContentValue.length}")
                                logger.error("\nSegments:\n${nextSegments}")
                                logger.error("\n\nContent:\n${nextFileContentValue}")

                                val tolerance = 8
                                require(newSegmentEndIndex + tolerance <= nextFileContentValue.length)
                            }
                        }
                        if (usedSegmentContentValue == null) {
                            require(newSegmentStartIndex == newSegmentEndIndex)
                        }
                    }


                }

                nextSegments to nextFileContent
            }

            val filledFileSegmentTree = FileSegmentOperationUtils.fillSparseIntervals(newFileSegments)

            val newFileSegmentPartials =
                immutableLinkedMapOfPartials(filledFileSegmentTree.toPartials(partialObjectGraph))
            targetFile.fileSegments = newFileSegmentPartials
            targetFile.content = newFileContent

            if (newFileContent.value != targetFileContent.value) {
                logger.info("Writing actual update to ${targetFile.filePath?.value}: ${newFileContent.value?.length} bytes from ${targetFileContent.value?.length} bytes")
                logger.info("Old content:\n========\n${targetFileContent.value}\n--------")
                logger.info("New content:\n--------\n${newFileContent.value}\n========")
            }

            // As the final step, do a mutating re-segment with the proper segmenter.
            val reSegmentMono = if (reSegmentAfterIncorporation) {
                val reSegmentGraphMorphism = ReSegmentGraphMorphism(
                    treeParseFunctions,
                    partialObjectGraph,
                    currentFileSegmentOp,
                    targetFile,
                    newFileSegmentContentType,
                )

                reSegmentGraphMorphism.updateFileSegments()
            } else {
                Mono.empty()
            }

            reSegmentMono.then(
                nonBlocking {
                    // Pick any updated file segment for the `implementedFileSegment`, either an explicitly updated
                    // segment that survived re-segmenting, or a newly created segment post re-segmenting
                    val finalFileSegmentUuids = targetFile.fileSegments?.values?.map { it.uuid }?.toSet() ?: emptySet()
                    val someUpdatedFileSegmentUuid = segmentsForContentUpdate.firstOrNull { (uuid, _) ->
                        uuid in finalFileSegmentUuids
                    }?.first ?: finalFileSegmentUuids.firstOrNull { it !in initialFileSegments }

                    if (someUpdatedFileSegmentUuid != null) {
                        currentFileSegmentOp.implementedFileSegment = PartialRef(
                            someUpdatedFileSegmentUuid
                        )
                    } else {
                        logger.warn("File segment graph morphism failed to produce an implemented file segment")

                        // This means we failed to produce an implementation, but we are obligated to complete the
                        // operation and move on regardless
                        currentFileSegmentOp.implementedFileSegment = PartialRef(null)
                    }

                    Invariants.check { require ->
                        require(FileSegmentTree.ofFile(targetFile) != null)
                    }
                }
            ).then()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SegmentEditGraphMorphism::class.java)
    }
}
