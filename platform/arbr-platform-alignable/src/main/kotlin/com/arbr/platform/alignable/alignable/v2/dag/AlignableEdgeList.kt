package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.ml.optimization.base.ParameterValue
import com.arbr.platform.ml.optimization.base.ParameterValueProvider

class AlignableEdgeList<L : MetricAlignable<L, LOp>, LOp : Any>(
    val alignmentCache: AlignmentCache<L, LOp>,
    val parameterValueProvider: ParameterValueProvider,
    val elements: List<NormativeEdge<L, LOp>>,
) : MetricAlignable<AlignableEdgeList<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>> {
    override fun applyAlignment(alignmentOperations: List<NormativeEdgeAlignmentOperation<L, LOp>>): AlignableEdgeList<L, LOp> {
        val elementsByLabel = elements
            .associateBy { v ->
                v.label
            }
            .toMutableMap()

        for (op in alignmentOperations) {
            val sourceElement = op.labelAlignment.sourceElement
            val targetElement = op.labelAlignment.targetElement
            if (sourceElement in elementsByLabel) {
                val targetEdge = elementsByLabel[sourceElement]!!
                val nextEdge = targetEdge.applyAlignment(listOf(op))
                elementsByLabel[sourceElement] = nextEdge
            } else {
                elementsByLabel[targetElement] = NormativeEdge(
                    alignmentCache,
                    parameterValueProvider,
                    targetElement,
                    op.netNorm
                )
            }
        }

        val nextLabelList = elementsByLabel.values.toList() // .filter { it.norm.evaluate() > 0 }
        return AlignableEdgeList(alignmentCache, parameterValueProvider, nextLabelList)
    }

    override fun align(e: AlignableEdgeList<L, LOp>): MetricAlignment<AlignableEdgeList<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>> {
        // Simple greedy match, strict equality
        val matchedEdges = mutableSetOf<Int>()
        val matchedOtherEdges = mutableSetOf<Int>()
        val matches = mutableListOf<MetricAlignment<NormativeEdge<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>>>()
        for ((i, edge) in elements.withIndex()) {
            if (i in matchedEdges) {
                continue
            }

            for ((j, otherEdge) in e.elements.withIndex()) {
                if (j in matchedOtherEdges) {
                    continue
                }

                val alignment: MetricAlignment<NormativeEdge<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>> =
                    edge.align(otherEdge)
                matches.add(alignment)
            }
        }

        val unmatchedEdges = elements.withIndex()
            .filter { (i, _) -> i !in matchedEdges }
            .map { it.value }
        val unmatchedOtherEdges = e.elements.withIndex()
            .filter { (i, _) -> i !in matchedOtherEdges }
            .map { it.value }

        val deletions = unmatchedEdges.map { it.align(it.empty()) }
        val insertions = unmatchedOtherEdges.map { it.empty().align(it) }

        val allOperations = deletions.flatMap { it.operations } + insertions.flatMap { it.operations }
        val totalCost = (deletions + insertions).fold(ParameterValue(0.0)) { t, x ->
            x.costMetric + t
        }

        return MetricAlignment.of(
            parameterValueProvider,
            allOperations,
            totalCost,
            this,
            e,
        )
    }

    override fun empty(): AlignableEdgeList<L, LOp> {
        return AlignableEdgeList(
            alignmentCache,
            parameterValueProvider,
            emptyList()
        )
    }
}