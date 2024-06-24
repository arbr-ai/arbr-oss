package com.arbr.core_web_dev.util.file_segments.morphism

import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentContentType
import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.alignment.Alignment
import java.util.*

// TODO: Consolidate the 9000 different variants of file segment trees
data class AlignableSegmentIndexReprTreeNode(
    val contentType: SegmentContentType?,
    val ruleName: String?,
    val name: Optional<String>?,
    val elementIndex: Int?,
    val startIndex: Int?,
    val endIndex: Int?,
) : Alignable<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode> {
    override fun align(e: AlignableSegmentIndexReprTreeNode): Alignment<AlignableSegmentIndexReprTreeNode, AlignableSegmentIndexReprTreeNode> {
        if (this == e) {
            return Alignment.Equal(this, e)
        }

        val op = AlignableSegmentIndexReprTreeNode(
            contentType = if (contentType == e.contentType) null else e.contentType,
            ruleName = if (ruleName == e.ruleName) null else e.ruleName,
            name = if (name == e.name) null else e.name,
            elementIndex = if (elementIndex == e.elementIndex) null else e.elementIndex,
            startIndex = if (startIndex == e.startIndex) null else e.startIndex,
            endIndex = if (endIndex == e.endIndex) null else e.endIndex,
        )

        return Alignment.Align(
            listOf(op),
            1.0,
            this,
            e,
        )
    }

    override fun applyAlignment(alignmentOperations: List<AlignableSegmentIndexReprTreeNode>): AlignableSegmentIndexReprTreeNode {
        var elt = this
        for (op in alignmentOperations) {
            elt = elt.copy(
                contentType = op.contentType ?: elt.contentType,
                ruleName = op.ruleName ?: elt.ruleName,
                name = op.name ?: elt.name,
                elementIndex = op.elementIndex ?: elt.elementIndex,
                startIndex = op.startIndex ?: elt.startIndex,
                endIndex = op.endIndex ?: elt.endIndex,
            )
        }
        return elt
    }

    override fun empty(): AlignableSegmentIndexReprTreeNode {
        return AlignableSegmentIndexReprTreeNode(
            contentType = null,
            ruleName = null,
            name = null,
            elementIndex = null,
            startIndex = null,
            endIndex = null,
        )
    }
}
