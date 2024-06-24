package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.platform.alignable.alignable.diff.DiffOperationLineAlignmentOperation
import com.arbr.platform.alignable.alignable.v2.dag.*
import java.util.*

data class DocumentDiffAlignmentCache(

    override val dagNodeValueAlignmentCache: MutableMap<
            Pair<AlignableDiffOperation, AlignableDiffOperation>,
            Optional<MetricAlignment<AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
            > = mutableMapOf(),

    override val dagEdgeListAlignmentCache: MutableMap<
            Pair<AlignableEdgeList<AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableEdgeList<AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
            Optional<MetricAlignment<AlignableEdgeList<AlignableDiffOperation, DiffOperationLineAlignmentOperation>, NormativeEdgeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation>>>,
            > = mutableMapOf(),

    override val dagNodeOuterAlignmentCache: MutableMap<
            Pair<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>,
            Optional<MetricAlignment<AlignableDAGNode<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>, AlignableDAGNodeAlignmentOperation<AlignableDiffOperation, DiffOperationLineAlignmentOperation, AlignableDiffOperation, DiffOperationLineAlignmentOperation>>>,
            > = mutableMapOf(),
): AlignmentCache<AlignableDiffOperation, DiffOperationLineAlignmentOperation>