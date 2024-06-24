package com.arbr.core_web_dev.util.file_segments

import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.field.*
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.util_common.LexIntSequence
import com.arbr.util_common.invariants.Invariants
import org.slf4j.LoggerFactory
import java.util.*

object FileSegmentOperationUtils {

    private val logger = LoggerFactory.getLogger(FileSegmentOperationUtils::class.java)

    /**
     * Replace the contents located at the old indices of the given file segment with the given content, potentially
     * adjusting the boundaries of the file segment based on whitespace and formatting.
     * Reflect any changes in the returned values as (fileContent, segmentContent).
     *
     * The target indices for replacement (from the segment node) must be preserved, as this operation must not modify
     * any other segments surrounding the target.
     *
     * Returns a pair of new full content and the corrected segment content used for the substitution.
     */
    fun replaceRangeExclusive(
        fullContent: String,
        sourceStartIndex: Int,
        sourceEndIndex: Int,
        newSegmentContent: String,
    ): Pair<String, String> {
        Invariants.check { require ->
            require(sourceEndIndex <= fullContent.length)
        }

        val sourceContent = fullContent.substring(sourceStartIndex, sourceEndIndex)

        val sourceLeadingWhitespace = sourceContent.takeWhile { it.isWhitespace() }
        val sourceTrailingWhitespace = sourceContent.takeLastWhile { it.isWhitespace() }

        val newLeadingWhitespace = newSegmentContent.takeWhile { it.isWhitespace() }
        val newTrailingWhitespace = newSegmentContent.takeLastWhile { it.isWhitespace() }

        val sourcePrefixTerminatingCharacterIsWhitespaceOrFileStart = if (sourceStartIndex >= 1 && fullContent.length >= sourceStartIndex) {
            fullContent[sourceStartIndex - 1].isWhitespace()
        } else {
            // File start
            true
        }

        val sourceSuffixLeadingCharacterIsWhitespace = if (fullContent.length > sourceEndIndex) {
            fullContent[sourceEndIndex].isWhitespace()
        } else {
            // Might be EOF, in which case we do want whitespace
            false
        }

        val resultLeadingWhitespace = if (sourceLeadingWhitespace.length > newLeadingWhitespace.length) {
            sourceLeadingWhitespace
        } else if (newLeadingWhitespace.isEmpty() && !sourcePrefixTerminatingCharacterIsWhitespaceOrFileStart) {
            "\n"
        } else {
            newLeadingWhitespace
        }
        val resultTrailingWhitespace = if (sourceTrailingWhitespace.length > newTrailingWhitespace.length) {
            sourceTrailingWhitespace
        } else if (newTrailingWhitespace.isEmpty() && !sourceSuffixLeadingCharacterIsWhitespace) {
            "\n"
        } else {
            newTrailingWhitespace
        }
        val actualSegmentContent = resultLeadingWhitespace + newSegmentContent.trim() + resultTrailingWhitespace

        val newFullContent = fullContent.replaceRange(
            sourceStartIndex,
            sourceEndIndex,
            actualSegmentContent,
        )

        return Pair(newFullContent, actualSegmentContent)
    }

    /**
     * Given a file segment which already is placed among its siblings, but has not updated its indices, give the
     * corrected indices for the segment to reflect its new content, based on sibling indices or parent indices for
     * segments which are first among their siblings.
     */
    private fun newRange(
        fileSegmentTree: FileSegmentTree,
        fileSegmentNode: FileSegmentNode,
        newContent: ArbrFileSegmentOpContentValue,
    ): Pair<ArbrFileSegmentStartIndexValue, ArbrFileSegmentEndIndexValue>? {
        val nodes = fileSegmentTree.flatten()
        val siblings = nodes
            .filter { it.parents.parentSegment?.uuid == fileSegmentNode.parents.parentSegment?.uuid }

        val parent = nodes.firstOrNull { it.properties.uuid == fileSegmentNode.parents.parentSegment?.uuid }

        // Check new segment is among its siblings
        Invariants.check { require ->
            require(siblings.any { it.properties.uuid == fileSegmentNode.properties.uuid })
        }

        val indexOf = siblings.indexOfFirst { it.properties.uuid == fileSegmentNode.properties.uuid }
        return if (indexOf == -1) {
            null
        } else {
            val startIndex = if (indexOf == 0) {
                parent?.properties?.startIndex
            } else {
                siblings[indexOf - 1].properties.endIndex?.let { priorSiblingEnd ->
                    ArbrFileSegment.StartIndex.initialize(
                        priorSiblingEnd.kind,
                        priorSiblingEnd.value,
                        priorSiblingEnd.generatorInfo,
                    )
                }
            } ?: ArbrFileSegment.StartIndex.constant(0)

            startIndex to ArbrFileSegment.EndIndex.initialize(
                newContent.kind,
                startIndex.value + (newContent.value?.length ?: 0),
                newContent.generatorInfo,
            )
        }
    }

    private fun postMapChildren(
        node: FileSegmentNode,
        children: Map<String, List<HomogenousRootedListDAG<FileSegmentNode>>>,
        f: (FileSegmentNode, List<HomogenousRootedListDAG<FileSegmentNode>>) -> List<HomogenousRootedListDAG<FileSegmentNode>>,
    ): FileSegmentTree {
        val childTrees = children[fileSegmentsKey]
            ?: return FileSegmentTree(node, children)

        val mappedChildren = childTrees.map { childTree ->
            postMapChildren(childTree.nodeValue, childTree.children, f)
        }

        val newChildren = f(node, mappedChildren)

        return FileSegmentTree(node, mapOf(fileSegmentsKey to newChildren))
    }

    /**
     * Prune the children of an updated file segment.
     * Optionally remove the parent too (such as for deletion).
     */
    fun pruneFileSegmentDescendants(
        fileSegmentTree: FileSegmentTree,
        fileSegmentNode: FileSegmentNode,
        pruneParent: Boolean,
    ): FileSegmentTree {
        return postMapChildren(
            fileSegmentTree.nodeValue,
            fileSegmentTree.children,
        ) { _, newChildren ->
            newChildren.filter {
                if (pruneParent) {
                    it.nodeValue.parents.parentSegment?.uuid != fileSegmentNode.properties.uuid && it.nodeValue.properties.uuid != fileSegmentNode.properties.uuid
                } else {
                    it.nodeValue.parents.parentSegment?.uuid != fileSegmentNode.properties.uuid
                }
            }
        }
    }

    /**
     * Given a tree of target file segments, and a file segment node already placed within the tree, update the
     * indices of the file segments to account for the new content of the segment.
     */
    fun rectifyFileSegmentIndices(
        fileSegmentTree: FileSegmentTree,
        fileSegmentNode: FileSegmentNode,
        newContent: ArbrFileSegmentOpContentValue,
    ): FileSegmentTree {
        val (newStartIndex, newEndIndex) = newRange(fileSegmentTree, fileSegmentNode, newContent)!!

        val newContentLength = newContent.value?.length ?: 0

        val oldLength = (fileSegmentNode.properties.endIndex?.value
            ?: 0) - (fileSegmentNode.properties.startIndex?.value ?: 0)
        val lengthDiff = newContentLength - oldLength

        // Every segment is either pre, post, or contains target segment
        // Only 3 valid states but represent as Pair<Boolean, Boolean> = (start <= targetStart, end >= targetEnd)
        var seenSegment = false
        val indexAdjustedFileSegmentDag = fileSegmentTree.mapPrePost(
            { baseFileSegment ->
                val properties = if (seenSegment) {
                    // Extend start index
                    baseFileSegment.properties.copy(
                        startIndex = baseFileSegment.properties.startIndex!!.map { it + lengthDiff },
                    )
                } else if (baseFileSegment.properties.uuid == fileSegmentNode.properties.uuid) {
                    seenSegment = true

                    baseFileSegment.properties.copy(
                        startIndex = newStartIndex,
                        endIndex = newEndIndex
                    )
                } else {
                    baseFileSegment.properties
                }

                baseFileSegment.copy(properties = properties)
            }
        ) { premappedFileSegment, _ ->
            if (premappedFileSegment.properties.uuid == fileSegmentNode.properties.uuid || !seenSegment) {
                premappedFileSegment
            } else {
                // Extend end index
                premappedFileSegment.copy(
                    properties = premappedFileSegment.properties.copy(
                        endIndex = premappedFileSegment.properties.endIndex!!.map { it + lengthDiff },
                    ),
                )
            }
        }

        return FileSegmentTree(indexAdjustedFileSegmentDag.nodeValue, indexAdjustedFileSegmentDag.children)
    }

    private fun paddingFileSegmentNode(
        file: PartialRef<out ArbrFile, PartialFile>?,
        parentFileSegment: PartialRef<out ArbrFileSegment, PartialFileSegment>?,
        contentType: ArbrFileSegmentContentTypeValue?,
        startIndex: Long,
        endIndex: Long,
    ): FileSegmentNode {
        val uuid = UUID.randomUUID().toString()
        val properties = FileSegmentProperties(
            uuid = uuid,
            contentType = contentType,
            ruleName = ArbrFileSegment.RuleName.materialized("miscellaneous"),
            name = ArbrFileSegmentNameValue.materialized(null),
            elementIndex = ArbrFileSegmentElementIndexValue.materialized(0), // TODO: Compute via dfs
            startIndex = ArbrFileSegmentStartIndexValue.materialized(startIndex),
            endIndex = ArbrFileSegmentEndIndexValue.materialized(endIndex),
            summary = null,
            containsTodo = null,
        )
        val parents = FileSegmentParents(
            file = file,
            parentSegment = parentFileSegment,
        )
        val heterogenousChildren = FileSegmentHeterogenousChildren(
            fileSegmentOps = null,
        )

        return FileSegmentNode(
            properties = properties,
            parents = parents,
            heterogenousChildren = heterogenousChildren
        )
    }

    private fun fillChildIntervals(
        parent: FileSegmentNode,
        subgraphs: List<HomogenousRootedListDAG<FileSegmentNode>>,
        intervalStart: Long,
        intervalEnd: Long,
    ): List<HomogenousRootedListDAG<FileSegmentNode>> {
        // Empty child intervals must be valid, or else trees would need to continue indefinitely
        if (subgraphs.isEmpty()) {
            return subgraphs
        }

        val augmentedChildren = mutableListOf<HomogenousRootedListDAG<FileSegmentNode>>()
        var childIntervalEndIndex = intervalStart
        for (subgraph in subgraphs) {
            val startIndex = subgraph.nodeValue.properties.startIndex
            val endIndex = subgraph.nodeValue.properties.endIndex
            if (startIndex == null || endIndex == null) {
                logger.error("Unable to fill child intervals: missing index on ${subgraph.nodeValue.properties.uuid}")
                return subgraphs
            }

            if (startIndex.value > childIntervalEndIndex) {
                // Add a padding interval
                val newChild = paddingFileSegmentNode(
                    subgraph.nodeValue.parents.file,
                    subgraph.nodeValue.parents.parentSegment,
                    subgraph.nodeValue.properties.contentType,
                    childIntervalEndIndex,
                    startIndex.value,
                )


                augmentedChildren.add(
                    FileSegmentTree(
                        newChild,
                        mapOf(fileSegmentsKey to emptyList()),
                    )
                )
            }

            childIntervalEndIndex = endIndex.value
            augmentedChildren.add(subgraph)
        }

        if (childIntervalEndIndex < intervalEnd) {
            val lastNode = subgraphs.lastOrNull()
            val newSegment = if (lastNode == null) {
                paddingFileSegmentNode(
                    parent.parents.file,
                    PartialRef(parent.properties.uuid),
                    parent.properties.contentType,
                    childIntervalEndIndex,
                    intervalEnd,
                )
            } else {
                paddingFileSegmentNode(
                    lastNode.nodeValue.parents.file,
                    lastNode.nodeValue.parents.parentSegment,
                    lastNode.nodeValue.properties.contentType,
                    childIntervalEndIndex,
                    intervalEnd,
                )
            }

            augmentedChildren.add(
                FileSegmentTree(
                    newSegment,
                    mapOf(fileSegmentsKey to emptyList()),
                )
            )
        }

        return augmentedChildren
    }

    /**
     * Fill any intervals in the tree such that each child sequence spans its whole parent interval.
     */
    fun fillSparseIntervals(
        fileSegmentTree: FileSegmentTree,
    ): FileSegmentTree {
//        HomogenousRootedListDAG.fromGenerator(
//            fileSegmentTree,
//            { it.nodeValue }
//        ) { tree ->
//            tree.children.mapValues { (_, childList) ->
//
//            }
//        }

        return postMapChildren(
            fileSegmentTree.nodeValue,
            fileSegmentTree.children,
        ) { node, mappedChildren ->
            val intervalStart = node.properties.startIndex?.value
            val intervalEnd = node.properties.endIndex?.value

            if (intervalStart == null || intervalEnd == null) {
                mappedChildren
            } else {
                fillChildIntervals(
                    node,
                    mappedChildren,
                    intervalStart,
                    intervalEnd,
                )
            }
        }
    }

    /**
     * Invariant check for debugging/testing
     */
    fun coversContent(
        filePath: ArbrFileFilePathValue,
        fileContent: ArbrFileContentValue,
        fileSegments: List<PartialFileSegment>,
        fail: (String) -> Nothing,
    ) {
        val length = fileContent.value?.length

        if (length == null) {
            if (fileSegments.size != 1) {
                fail("Expected 1 segment for empty file, got ${fileSegments.size}")
            }
        } else {
            val rootFileSegments = fileSegments.filter { it.parentSegment == null }

            val rootFileSegment = if (rootFileSegments.isEmpty()) {
                fail("No root file segment after operation")
            } else if (rootFileSegments.size > 1) {
                fail("Multiple root file segments after operation")
            } else {
                rootFileSegments.first()
            }

            if (rootFileSegment.startIndex?.value != 0L) {
                fail("Root FS not starting at 0")
            } else if (rootFileSegment.endIndex?.value != length.toLong()) {
                fail("Root FS file content length mismatch")
            }

            val seen = mutableSetOf<String>()
            var frontier = listOf(rootFileSegment)
            while (frontier.isNotEmpty()) {
                val nextFrontier = mutableListOf<PartialFileSegment>()
                for (fs in frontier) {
                    if (fs.uuid in seen) {
                        continue
                    }
                    seen.add(fs.uuid)

                    val children = fs.fileSegments?.values?.toList()
                        ?: fail("File segment has null children instead of empty")

                    // Check children fill up FS' interval
                    // Do NOT enforce ordered
                    var i = fs.startIndex?.value
                        ?: fail("Missing start interval for parent segment")
                    val parentEndIndex = fs.endIndex?.value
                        ?: fail("Missing end interval for parent segment")

                    val sortedChildren = sortedByIntervals(children)
                    for (fileSegment in sortedChildren) {
                        if (fileSegment.startIndex?.value != i) {
                            fail("Expected interval start at $i but got ${fileSegment.startIndex?.value}")
                        }
                        val newEndIndex = fileSegment.endIndex?.value
                            ?: fail("Missing end interval for segment")
                        i = newEndIndex
                    }

                    // TODO: New generation case
                    if (children.isNotEmpty() && i != parentEndIndex) {
                        fail("Final child segment in range did not match parent interval end: Expected $parentEndIndex, got $i")
                    }

                    nextFrontier.addAll(
                        children.filter { it.uuid !in seen }
                    )
                    seen.addAll(nextFrontier.map { it.uuid })
                }

                frontier = nextFrontier
            }
        }

        logger.info("File segments Passed checks on file ${filePath.value} with $length bytes")
    }

    fun sortedByIntervals(fileSegments: List<PartialFileSegment>): List<PartialFileSegment> {
        return fileSegments.sortedBy {
            val startIndex = it.startIndex?.value?.toInt() ?: Int.MAX_VALUE
            val endIndex = it.endIndex?.value?.toInt() ?: Int.MAX_VALUE

            LexIntSequence(listOf(startIndex, -endIndex))
        }
    }

    private const val fileSegmentsKey = "DdlGithub.FileSegment.ParentSegment"

}