package com.arbr.core_web_dev.util.file_segments

import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.util_common.invariants.Invariants
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import org.slf4j.LoggerFactory

data class FileSegmentTree(
    override val nodeValue: FileSegmentNode,
    override val children: Map<String, List<HomogenousRootedListDAG<FileSegmentNode>>>,
) : HomogenousRootedListDAG<FileSegmentNode> {

    fun toPartials(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
    ): List<PartialFileSegment> {

        val partialMap = linkedMapOf<String, PartialFileSegment>()

        // uuid -> [uuid]
        val nodeMap = mutableMapOf<String, FileSegmentNode>()
        val dependencyMap = linkedMapOf<String, MutableList<String>>()
        val seenParentUuids = mutableSetOf<String>()
        fun populateDependencies(tree: HomogenousRootedListDAG<FileSegmentNode>) {
            val thisNodeUuid = tree.nodeValue.properties.uuid
            if (thisNodeUuid in seenParentUuids) {
                return
            }
            seenParentUuids.add(thisNodeUuid)

            nodeMap[thisNodeUuid] = tree.nodeValue

            // Note in this case parents depend on children
            val dependencies = tree.children.flatMap { (_, chl) ->
                chl.map { ch ->
                    ch.nodeValue.properties.uuid
                }
            }
            dependencyMap[thisNodeUuid] = dependencies.toMutableList()

            tree.children.forEach { (_, chl) ->
                chl.forEach { populateDependencies(it) }
            }
        }
        populateDependencies(this)

        while (dependencyMap.isNotEmpty()) {
            val satisfied = dependencyMap.filter { (_, dependencies) ->
                dependencies.all { it in partialMap }
            }.keys.toList()

            Invariants.check { require ->
                require(satisfied.isNotEmpty())
            }
            if (satisfied.isEmpty()) {
                logger.error("File segment tree contains a cycle or otherwise failed to satisfy dependencies during partial rendering")
                break
            }

            for (uuid in satisfied) {
                val nodeValue = nodeMap[uuid]!!
                val childPartials = dependencyMap[uuid]!!.map {
                    partialMap[it]!!
                }

                val partial = partialObjectGraph.get(uuid) ?: run {
                    logger.info("Creating new File Segment via tree: $uuid")

                    PartialFileSegment(
                        partialObjectGraph,
                        uuid = uuid,
                    )
                }

                partial.apply {
                    parent = nodeValue.parents.file
                    parentSegment = nodeValue.parents.parentSegment
                    contentType = nodeValue.properties.contentType
                    ruleName = nodeValue.properties.ruleName
                    name = nodeValue.properties.name
                    elementIndex = nodeValue.properties.elementIndex
                    startIndex = nodeValue.properties.startIndex
                    endIndex = nodeValue.properties.endIndex
                    summary = nodeValue.properties.summary
                    containsTodo = nodeValue.properties.containsTodo
                    fileSegments = immutableLinkedMapOfPartials(
                        childPartials
                    )
                    fileSegmentOps = nodeValue.heterogenousChildren.fileSegmentOps
                }
                partialMap[uuid] = partial

                dependencyMap.remove(uuid)
            }
        }

        // Return in DFS-order by flattening and mapping
        return flatten().map { partialMap[it.properties.uuid]!! }
    }

    fun getNode(uuid: String): FileSegmentNode? {
        return getNode(uuid, nodeValue, children, mutableSetOf())
    }

    override fun toString(): String {
        val nodeLine = "(${nodeValue.properties.startIndex?.value}, ${nodeValue.properties.endIndex?.value}) [ ${
            mapper.writeValueAsString(nodeValue)
        } ]"
        val children = children.values.joinToString("\n") { chl ->
            chl.joinToString("\n") {
                FileSegmentTree(it.nodeValue, it.children).toString().split("\n").joinToString("\n") { c -> "  $c" }
            }
        }
        return nodeLine + "\n" + children
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FileSegmentTree::class.java)


        private val mapper = jacksonObjectMapper()

        fun ofFile(
            file: PartialFile,
        ): FileSegmentTree? {
            val fileSegments = file.fileSegments?.values?.toList()
                ?: return null
            val fileSegmentMap = fileSegments.associateBy { it.uuid }

            val rootFileSegments = fileSegments.filter { it.parentSegment == null }
            val rootFileSegment = if (rootFileSegments.isEmpty()) {
                return null
            } else if (rootFileSegments.size > 1) {
                return null
            } else {
                rootFileSegments.first()
            }

            val dag = HomogenousRootedListDAG.fromGenerator(
                rootFileSegment,
                { FileSegmentNode.ofPartial(it) },
            ) { g ->
                // Here we face the same problem this class is meant to address - there are parallel representations
                // of the file segment tree in the models at the root level of the file and the deep graph extending
                // from the root node
                // We choose, somewhat arbitrarily, to take the segments at the root of the file, since there are more
                // implementations at the time of writing keeping those up to date
                // The full solution is to manage all resources with partial object graphs that limit duplicate
                // instances
                val children = g.fileSegments?.mapNotNull { (uuid, child) ->
                    val topLevelRegisteredChild = fileSegmentMap[uuid]

//                    Invariants.check { require ->
//                        if (topLevelRegisteredChild == null) {
//                            val yamlMapper = Mappers.yamlMapper
//                            logger.info("Toplevel file segments:\n ${fileSegments.joinToString("\n") { yamlMapper.writeValueAsString(it) }}")
//                            logger.info("This segment's children:\n ${g.fileSegments?.values?.joinToString("\n") { yamlMapper.writeValueAsString(it) }}")
//                        }
//                        require(topLevelRegisteredChild != null)
//                    }

                    if (topLevelRegisteredChild == null) {
                        // TODO: DIAGNOSE
                        logger.error("FILE SEGMENT $uuid NOT REGISTERED IN TOP LEVEL")
                        logger.info(file.filePath?.value ?: "")
                    }

                    topLevelRegisteredChild
                }
                    ?.let { unorderedSegments ->
                        FileSegmentOperationUtils.sortedByIntervals(unorderedSegments)
                    }
                    ?: emptyList()

                mapOf(fileSegmentsKey to children)
            }
            return FileSegmentTree(dag.nodeValue, dag.children)
        }

        private fun getNode(
            uuid: String,
            currentNode: FileSegmentNode,
            children: Map<String, List<HomogenousRootedListDAG<FileSegmentNode>>>,
            seenUuids: MutableSet<String>,
        ): FileSegmentNode? {
            val thisUuid = currentNode.properties.uuid
            if (thisUuid == uuid) {
                return currentNode
            }

            if (thisUuid in seenUuids) {
                return null
            }
            seenUuids.add(thisUuid)

            return children.values.firstNotNullOfOrNull { childList ->
                childList.firstNotNullOfOrNull { subgraph ->
                    getNode(uuid, subgraph.nodeValue, subgraph.children, seenUuids)
                }
            }
        }

        private const val fileSegmentsKey = "DdlGithub.FileSegment.ParentSegment"
    }

}