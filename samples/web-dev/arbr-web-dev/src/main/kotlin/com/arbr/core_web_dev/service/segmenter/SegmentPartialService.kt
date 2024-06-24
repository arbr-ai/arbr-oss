package com.arbr.core_web_dev.service.segmenter

import com.arbr.engine.util.immutableLinkedMapOfPartials
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentIndexReprTree
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.field.*
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import org.springframework.stereotype.Component
import java.util.*

@Component
class SegmentPartialService {

    private val segmenterService = SegmenterService()

    private fun fileSegment(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        fileUuid: String,
        parentFileSegment: PartialFileSegment?,
        repr: SegmentIndexReprTree,
    ): PartialFileSegment {
        return PartialFileSegment(
            partialObjectGraph,
            UUID.randomUUID().toString(),
        ).apply {
            parent = PartialRef(fileUuid)
            parentSegment = parentFileSegment?.let { PartialRef(it.uuid) }
            contentType = ArbrFileSegment.ContentType.materialized(repr.contentType.serializedName)
            ruleName = ArbrFileSegment.RuleName.materialized(repr.ruleName)
            name = ArbrFileSegmentNameValue.materialized(repr.name)
            elementIndex = ArbrFileSegmentElementIndexValue.materialized(repr.elementIndex.toLong())
            startIndex = ArbrFileSegmentStartIndexValue.materialized(repr.startIndex.toLong())
            endIndex = ArbrFileSegmentEndIndexValue.materialized(repr.endIndex.toLong())
        }
    }

    private fun segmentTreeToPartials(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        fileUuid: String,
        segmentReprTree: SegmentIndexReprTree,
    ): List<PartialFileSegment> {
        val graph = HomogenousRootedListDAG.fromGenerator(
            segmentReprTree,
            {
                it
            },
            {
                mapOf("children" to it.childElements)
            }
        )

        val segmentGraph: HomogenousRootedListDAG<PartialFileSegment> =
            graph.mapWithParent<PartialFileSegment> { segmentIndexReprTree, parent ->
                fileSegment(
                    partialObjectGraph,
                    fileUuid,
                    parent,
                    segmentIndexReprTree,
                )
            }.mapPrePost(
                { it }
            ) { fs, children ->
                fs.fileSegments = immutableLinkedMapOfPartials(children["children"]?.map { it.nodeValue } ?: emptyList())
                fs
            }

        return segmentGraph.flatten()
    }

    fun computeSegments(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        fileUuid: String,
        filePath: ArbrFileFilePathValue,
        fileContent: ArbrFileContentValue,
    ): List<PartialFileSegment> {
        // TODO: Use syntax errors
        val segmentReprTree = segmenterService.segmentFileContentUniversal(
            filePath.value,
            fileContent.value ?: "",
        ).segmentReprTree

        return segmentTreeToPartials(partialObjectGraph, fileUuid, segmentReprTree)
    }
}
