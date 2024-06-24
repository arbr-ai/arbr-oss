package com.arbr.core_web_dev.util.file_segments

import com.arbr.object_model.core.partial.PartialFileSegment

data class FileSegmentNode(
    val properties: FileSegmentProperties,
    val parents: FileSegmentParents,
    val heterogenousChildren: FileSegmentHeterogenousChildren,
) {

    companion object {
        fun ofPartial(partial: PartialFileSegment): FileSegmentNode {
            val properties = FileSegmentProperties(
                uuid = partial.uuid,
                contentType = partial.contentType,
                ruleName = partial.ruleName,
                name = partial.name,
                elementIndex = partial.elementIndex,
                startIndex = partial.startIndex,
                endIndex = partial.endIndex,
                summary = partial.summary,
                containsTodo = partial.containsTodo,
            )
            val parents = FileSegmentParents(
                file = partial.parent,
                parentSegment = partial.parentSegment,
            )
            val heterogenousChildren = FileSegmentHeterogenousChildren(
                fileSegmentOps = partial.fileSegmentOps,
            )
            return FileSegmentNode(properties, parents, heterogenousChildren)
        }
    }
}