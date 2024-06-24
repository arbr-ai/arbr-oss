package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.DiffOperationKind
import com.arbr.platform.alignable.alignable.NoViableAlignmentException
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.platform.alignable.alignable.diff.Chain
import com.arbr.platform.alignable.alignable.diff.DiffOperationLineAlignmentOperation
import com.arbr.platform.alignable.alignable.v2.dag.*
import com.arbr.ml.optimization.base.ParameterValueProvider
import java.util.*
import kotlin.jvm.optionals.getOrNull

class DocumentDiffAlignmentInducer(
    private val alignmentCache: DocumentDiffAlignmentCache,
    val parameterValueProvider: ParameterValueProvider,
) : ConstructiveDAGStateInducer<
        Chain<AlignableDiffOperation>,
        AlignableDiffOperation,
        DiffOperationLineAlignmentOperation
        > {

    override val initialState: Chain<AlignableDiffOperation> = Chain(null, null, 41928748)

    override val initialStateCode: Int = initialState.hashCode

    private fun baseAlignment(
        node0: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
        node1: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        return alignmentCache.dagNodeOuterAlignmentCache.computeIfAbsent(node0 to node1) {
            Optional.ofNullable(node0.alignOrNull(node1))
        }.getOrNull() ?: throw NoViableAlignmentException("DAG node", node0, node1)
    }

    private fun costAdjustedAlignment(
        kind: TokenWindowDAGAlignmentKind,
        outerKind: AdjacencyMatrixDAGAlignmentKind,
        alignment: MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        val operationCost = parameterValueProvider.getParameterValue(kind.costMetricKind())

//        val combinationKind = ADJ_TKW_COMBINATION_METRIC_MAP[outerKind.costMetricKind().name]?.get(kind.costMetricKind().name)
//            ?: throw Exception("No combination ${outerKind.costMetricKind().name} ${kind.costMetricKind().name}")
//        val combinationParameter = parameterValueProvider.getParameterValue(combinationKind)

        return MetricAlignment.of(
            parameterValueProvider,
            alignment.operations,
//            combinationParameter + (operationCost * alignment.costMetric),
            operationCost * alignment.costMetric,
            alignment.sourceElement,
            alignment.targetElement,
        )
    }

    private fun applyAlignment(
        targetOperation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        val modifier = when (targetOperation.node.kind) {
            DiffOperationKind.NUL -> 1.0
            DiffOperationKind.ADD -> 0.1
            DiffOperationKind.NOP -> 1.0
            DiffOperationKind.DEL -> 1.0
        }
        val baseAlignment = baseAlignment(targetOperation, targetOperation.empty())
        val modifiedAlignment = MetricAlignment.of(
            parameterValueProvider,
            baseAlignment.operations,
            baseAlignment.costMetric * modifier,
            baseAlignment.sourceElement,
            baseAlignment.targetElement,
        )

        return costAdjustedAlignment(
            TokenWindowDAGAlignmentKind.APPLY,
            AdjacencyMatrixDAGAlignmentKind.INDUCE,
            modifiedAlignment
        )
    }

    private fun dropAlignment(
        targetOperation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        return costAdjustedAlignment(
            TokenWindowDAGAlignmentKind.DROP,
            AdjacencyMatrixDAGAlignmentKind.INDUCE,
            baseAlignment(targetOperation.empty(), targetOperation),
        )
    }

    private fun skipAlignment(
        fromState: Chain<AlignableDiffOperation>,
        sourceOperation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
        targetIsNonEmpty: Boolean,
        targetIsComplete: Boolean,
        sourceIsNonEmpty: Boolean,
        sourceIsComplete: Boolean,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        val kind = if (!targetIsNonEmpty) {
            TokenWindowDAGAlignmentKind.SKIP_PRE
        } else if (targetIsComplete) {
            TokenWindowDAGAlignmentKind.SKIP_POST
        } else {
            TokenWindowDAGAlignmentKind.SKIP
        }

        // We want to retain the NOP code to ensure the result is serialized
        val skipTargetOperation = AlignableDAGNode(
            alignmentCache,
            parameterValueProvider,
            sourceOperation.node.empty().let { e ->
                e.copy(
                    diffOperation = e.diffOperation.copy(
                        kind = DiffOperationKind.NOP
                    )
                )
            },
            sourceOperation.parentEdges.empty(),
        )
        return costAdjustedAlignment(
            kind,
            AdjacencyMatrixDAGAlignmentKind.DEDUCE,
            baseAlignment(skipTargetOperation, sourceOperation),
        )
    }

    private fun mergeAlignment(
        targetOperation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
        sourceOperation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>,
    ): MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>> {
        val alignment = baseAlignment(targetOperation, sourceOperation)
        val kind =
            if (alignment is MetricAlignment.Equal) TokenWindowDAGAlignmentKind.MATCH else TokenWindowDAGAlignmentKind.EDIT
        return costAdjustedAlignment(
            kind,
            AdjacencyMatrixDAGAlignmentKind.MERGE,
            alignment,
        )
    }

    override fun viableOperations(
        fromState: Chain<AlignableDiffOperation>,
        targetOperation: Optional<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
        targetIsNonEmpty: Boolean,
        targetIsComplete: Boolean,
        sourceOperation: Optional<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
        sourceIsNonEmpty: Boolean,
        sourceIsComplete: Boolean
    ): List<MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>> {
        val operations =
            mutableListOf<
                    MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>
                    >()

        if (targetOperation.isPresent && sourceOperation.isPresent) {
            operations.add(
                mergeAlignment(targetOperation.get(), sourceOperation.get())
            )
        } else if (targetOperation.isPresent) {
            // APPLY, INDUCE, DROP
            val targetOp = targetOperation.get()
            operations.add(
                dropAlignment(targetOp)
            )
            operations.add(
                applyAlignment(targetOp)
            )

//            operations.addAll(
//                viability.inducibleOperations(fromState, targetOp)
//            )
        } else if (sourceOperation.isPresent) {
            // DEDUCE, SKIP*
            val sourceOp = sourceOperation.get()
            operations.add(
                skipAlignment(
                    fromState,
                    sourceOp,
                    targetIsNonEmpty,
                    targetIsComplete,
                    sourceIsNonEmpty,
                    sourceIsComplete,
                )
            )

//            operations.addAll(
//                viability.deducibleOperations(fromState, sourceOp)
//            )
        } else {
            //
        }

        return operations
    }

    override fun apply(
        state: Chain<AlignableDiffOperation>,
        stateCode: Int,
        operation: AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>
    ): Pair<Chain<AlignableDiffOperation>, Int> {
        val nextState = stateCode * 31 + operation.node.hashCode()
        return state.and(operation.node) to nextState
    }
}
