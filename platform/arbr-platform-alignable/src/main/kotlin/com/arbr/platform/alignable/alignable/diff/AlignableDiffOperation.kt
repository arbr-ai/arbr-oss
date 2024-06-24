package com.arbr.platform.alignable.alignable.diff

import com.arbr.content_formats.format.DiffOperation
import com.arbr.content_formats.format.DiffOperationKind
import com.arbr.platform.alignable.alignable.DIFF_OP_BASELINE
import com.arbr.platform.alignable.alignable.DIFF_OP_COST_METRIC_MATRIX
import com.arbr.platform.alignable.alignable.STR_NORM
import com.arbr.platform.alignable.alignable.STR_WEIGHT
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.alignable.alignable.edit_operation.AlignableEditOperation
import com.arbr.platform.ml.optimization.base.ParameterValue
import com.arbr.platform.ml.optimization.base.ParameterValueProvider
import org.apache.commons.text.similarity.LevenshteinDistance

data class AlignableDiffOperation(
    val diffOperation: DiffOperation,
    val parameterValueProvider: ParameterValueProvider,
) : AlignableEditOperation<Chain<AlignableDiffOperation>, AlignableDiffOperation, DiffOperationLineAlignmentOperation> {

    val kind get() = diffOperation.kind
    val lineContent get() = diffOperation.lineContent
    val lineNumber get() = diffOperation.lineNumber

    private fun kindAlignmentCost(documentKind: DiffOperationKind, patchKind: DiffOperationKind): ParameterValue {
        val metricKind = DIFF_OP_COST_METRIC_MATRIX[documentKind.ordinal][patchKind.ordinal]
        return parameterValueProvider.getParameterValue(metricKind)
    }

    override fun applyAlignment(alignmentOperations: List<DiffOperationLineAlignmentOperation>): AlignableDiffOperation {
        val lastOp = alignmentOperations.lastOrNull()
        val aKind: DiffOperationKind = lastOp?.kind ?: kind
        val aContent = lastOp?.stringResult ?: lineContent
        val aDiffOperation = DiffOperation(
            aKind,
            aContent,
            lineNumber,
        )

        return AlignableDiffOperation(
            aDiffOperation,
            parameterValueProvider,
        )
    }

    private fun stringDistanceTo(content: String, otherContent: String): Int {
        if (content.isBlank()) {
            return otherContent.length
        }
        if (otherContent.isBlank()) {
            return content.length
        }

        return levenshteinDistance.apply(content, otherContent)
    }

    override fun align(e: AlignableDiffOperation): MetricAlignment<AlignableDiffOperation, DiffOperationLineAlignmentOperation> {
        val kindAlignCost = kindAlignmentCost(e.kind, kind)

        // Trim strings before computing edit distance to relax leading and trailing whitespace constraints
        // Future: consider adding a cost per difference in whitespace
        val content = lineContent.trim()
        val otherContent = e.lineContent.trim()
        val stringDistance = stringDistanceTo(content, otherContent)

        val thisIsEmpty = kind == DiffOperationKind.NUL && lineContent.isEmpty() && lineNumber == -1
        val otherIsEmpty = e.kind == DiffOperationKind.NUL && e.lineContent.isEmpty() && e.lineNumber == -1

        return if (stringDistance == 0 && kind == e.kind) {
            MetricAlignment.Equal(parameterValueProvider, this, e)
        } else {
            // ADD overwrites other content when merged; all other operations acquiesce
            // Behavior of aligning to NUL should not matter since it's hidden
            val resultText = if (kind == DiffOperationKind.ADD && e.kind == DiffOperationKind.NUL) {
                lineContent
            } else {
                e.lineContent
            }

            val kindOps: List<DiffOperationLineAlignmentOperation> = if (kind == e.kind) {
                emptyList()
            } else {
                // Use the patch kind since the doc aligns to the patch
                listOf(DiffOperationLineAlignmentOperation(kind, null))
            }

            val nonemptyBaselineCost = if (thisIsEmpty == otherIsEmpty) {
                ParameterValue(0.0)
            } else {
                parameterValueProvider.getParameterValue(DIFF_OP_BASELINE) * 2.0
            }

            val stringEditCoefficient = if (thisIsEmpty == otherIsEmpty) {
                // Both empty or both nonempty - proper edit
                parameterValueProvider.getParameterValue(STR_WEIGHT)
            } else {
                // Norm
                parameterValueProvider.getParameterValue(STR_NORM)
            }

            val stringEditCost = if (stringDistance == 0) {
                ParameterValue(0.0)
            } else {
                stringEditCoefficient * stringDistance.toDouble()
            }

            val stringOps = if (stringDistance == 0) {
                emptyList()
            } else {
                listOf(DiffOperationLineAlignmentOperation(null, resultText))
            }

            val totalCost = kindAlignCost + stringEditCost + nonemptyBaselineCost

            MetricAlignment.Align(
                parameterValueProvider,
                kindOps + stringOps,
                totalCost,
                this,
                e.copy(
                    diffOperation = e.diffOperation.copy(
                        kind = this.kind,
                        lineContent = resultText,
                    ),
                ),
            )
        }
    }

    override fun empty(): AlignableDiffOperation {
        // Line number -1 is a hack to identify the empty element
        return AlignableDiffOperation(DiffOperation(DiffOperationKind.NUL, "", -1), parameterValueProvider)
    }

    override fun applyTo(state: Chain<AlignableDiffOperation>): Chain<AlignableDiffOperation> {
        return state.and(this)
    }

    override fun toString(): String {
        val paddedLineContent = lineContent.padEnd(48)

        return "diff ${kind.name}@${lineNumber}[${paddedLineContent}]"
    }

    override fun hashCode(): Int {
        return (kind.hashCode() * 31).xor(lineContent.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlignableDiffOperation

        if (kind != other.kind) return false
        if (lineContent != other.lineContent) return false
        if (lineNumber != other.lineNumber) return false

        return true
    }

    companion object {
        private val levenshteinDistance = LevenshteinDistance.getDefaultInstance()
    }
}