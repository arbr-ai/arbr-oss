package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.NoViableAlignmentException
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.ml.optimization.base.ParameterValueProvider
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Alignable DAG node wrapping an alignable vertex type to add edge alignments.
 */
class AlignableDAGNode<T : MetricAlignable<T, TAO>, TAO, L : MetricAlignable<L, LOp>, LOp: Any>(
    val alignmentCache: AlignmentCache<L, LOp>,
    val parameterValueProvider: ParameterValueProvider,
    val node: T,
    val parentEdges: AlignableEdgeList<L, LOp>,
) : MetricAlignable<
        AlignableDAGNode<T, TAO, L, LOp>,
        AlignableDAGNodeAlignmentOperation<T, TAO, L, LOp>,
        > {
    override fun applyAlignment(alignmentOperations: List<AlignableDAGNodeAlignmentOperation<T, TAO, L, LOp>>): AlignableDAGNode<T, TAO, L, LOp> {
        val e1 = node.applyAlignment(alignmentOperations.mapNotNull { it.nodeOperation })
        val e2 = AlignableEdgeList(alignmentCache, parameterValueProvider, parentEdges.elements)
            .applyAlignment(alignmentOperations.mapNotNull { it.parentEdgeListOperation })
        return AlignableDAGNode(alignmentCache, parameterValueProvider, e1, e2)
    }

    override fun align(e: AlignableDAGNode<T, TAO, L, LOp>): MetricAlignment<AlignableDAGNode<T, TAO, L, LOp>, AlignableDAGNodeAlignmentOperation<T, TAO, L, LOp>> {
        val a1 = node.align(e.node)
        val a1Ops = a1.operations.map {
            AlignableDAGNodeAlignmentOperation<T, TAO, L, LOp>(it, null)
        }

        val a2 = alignmentCache.dagEdgeListAlignmentCache.computeIfAbsent(parentEdges to e.parentEdges) {
//            Optional.ofNullable(parentEdges.align(e.parentEdges))

            Optional.of(MetricAlignment.Equal(parameterValueProvider, parentEdges, e.parentEdges))
        }.getOrNull() ?: throw NoViableAlignmentException("Edges", parentEdges, e.parentEdges)

        val a2Ops = a2.operations.map {
            AlignableDAGNodeAlignmentOperation<T, TAO, L, LOp>(null, it)
        }

        val resultNode = AlignableDAGNode(
            alignmentCache,
            parameterValueProvider,
            a1.targetElement,
            a2.targetElement,
        )

        return if (a1 is MetricAlignment.Equal && a2 is MetricAlignment.Equal) {
            MetricAlignment.Equal(parameterValueProvider, this, resultNode)
        } else {
            MetricAlignment.Align(
                parameterValueProvider,
                a1Ops + a2Ops,
                a1.costMetric + a2.costMetric,
                this,
                resultNode,
            )
        }
    }

    override fun empty(): AlignableDAGNode<T, TAO, L, LOp> {
        return AlignableDAGNode(
            alignmentCache,
            parameterValueProvider,
            node.empty(),
            parentEdges.empty(),
        )
    }

    override fun toString(): String {
//        return "DAGNode[$node <- ${parentEdges.elements.map { it.label }}]"
        return "DAGNode[$node]"
    }

    private val hashCodeValue: Int by lazy {
        node.hashCode() * 31 + parentEdges.elements.hashCode()
    }

    override fun hashCode(): Int {
        return hashCodeValue
    }

    override fun equals(other: Any?): Boolean {
        return hashCodeValue == (other as? AlignableDAGNode<*, *, *, *>)?.hashCodeValue
    }
}