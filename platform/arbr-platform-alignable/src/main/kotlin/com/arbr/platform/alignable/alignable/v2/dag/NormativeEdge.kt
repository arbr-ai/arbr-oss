package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.ADJ_EDGE_GENERAL
import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.NoViableAlignmentException
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.ml.optimization.base.ParameterValue
import com.arbr.platform.ml.optimization.base.ParameterValueProvider
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs

class NormativeEdge<L : MetricAlignable<L, LOp>, LOp : Any>(
    private val alignmentCache: AlignmentCache<L, LOp>,
    private val parameterValueProvider: ParameterValueProvider,
    val label: Optional<L>,
    val norm: ParameterValue,
) : MetricAlignable<NormativeEdge<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>> {

    override fun applyAlignment(alignmentOperations: List<NormativeEdgeAlignmentOperation<L, LOp>>): NormativeEdge<L, LOp> {
        val finalNorm = alignmentOperations.fold(norm) { t, op ->
            t + op.netNorm
        }
        return alignmentOperations.lastOrNull()?.let { op ->
            NormativeEdge(
                alignmentCache,
                parameterValueProvider,
                op.labelAlignment.targetElement,
                finalNorm,
            )
        }
            ?: this
    }

    private fun getLabelAlignment(label0: L, label1: L): MetricAlignment<L, LOp> {
        return alignmentCache.dagNodeValueAlignmentCache.computeIfAbsent(label0 to label1) {
            Optional.ofNullable(label0.alignOrNull(label1))
        }.getOrNull() ?: throw NoViableAlignmentException("Edge node label", label0, label1)
    }

    override fun align(e: NormativeEdge<L, LOp>): MetricAlignment<NormativeEdge<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>> {
        // Edges require a slightly different perspective on cost:
        // In the sense that cost reflects non-conservative energy loss from state transitions, the cost associated
        // with edge transformations can be treated like either the activation energy required to achieve a higher
        // edge enthalpy, or the free energy dispersed by transition to a lower energy state.
        // The cost here is, importantly, only meant to model these transitions and not the states themselves.
        // For example: to align an edge set {E, F} with {E, F, G}, we ought to incur a cost proportional to the norm
        // of G to reflect the environmental energy required to add the edge for G, or else drop G entirely if this
        // cost is too high. Meanwhile, to align {E, F} to {E*, F}, where E* is some slight variation of E, we ought
        // to model the difference in norms between E and E* as the cost of transformation.
        // As a consequence of simply comparing norms, we need edge labels to be alignable as well, to prevent
        // permuting labels arbitrarily.

        val innerAlignment = if (label.isPresent && e.label.isPresent) {
            getLabelAlignment(label.get(), e.label.get())
        } else if (label.isPresent) {
            getLabelAlignment(label.get(), label.get().empty())
        } else if (e.label.isPresent) {
            getLabelAlignment(e.label.get(), e.label.get().empty())
        } else {
            return MetricAlignment.Equal(parameterValueProvider, this, e)
        }

        val labelAlignment = MetricAlignment.of(
            parameterValueProvider,
            innerAlignment.operations.map { Optional.of(it) },
            innerAlignment.costMetric,
            label,
            e.label,
        )

        return if (labelAlignment is MetricAlignment.Equal) {
            MetricAlignment.Equal(parameterValueProvider, this, e)
        } else {
            // TODO: Avoid unwrapping
            val eNorm = e.norm.value
            val thisNorm = norm.value
            val netNorm = eNorm - thisNorm
            val normAlignmentCost = abs(netNorm)

            val operation = NormativeEdgeAlignmentOperation(
                labelAlignment,
                ParameterValue(netNorm),
            )
            val costMetric = parameterValueProvider.getParameterValue(ADJ_EDGE_GENERAL) * normAlignmentCost

            MetricAlignment.Align(
                parameterValueProvider,
                listOf(operation),
                costMetric,
                this,
                e,
            )
        }
    }

    override fun empty(): NormativeEdge<L, LOp> {
        return NormativeEdge(alignmentCache, parameterValueProvider, label, ParameterValue(0.0))
    }

    override fun hashCode(): Int {
        return if (label.isPresent) {
            label.get().hashCode()
        } else {
            1346943
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NormativeEdge<*, *>

        return hashCode() == other.hashCode()
    }
}
