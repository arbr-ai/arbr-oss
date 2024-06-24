package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable
import com.arbr.platform.alignable.alignable.alignment.MetricAlignment
import java.util.*

interface AlignmentCache<L : MetricAlignable<L, LOp>, LOp: Any> {
    val dagNodeValueAlignmentCache: MutableMap<
            Pair<L, L>,
            Optional<MetricAlignment<L, LOp>>,
            >

    val dagEdgeListAlignmentCache: MutableMap<
            Pair<AlignableEdgeList<L, LOp>, AlignableEdgeList<L, LOp>>,
            Optional<MetricAlignment<AlignableEdgeList<L, LOp>, NormativeEdgeAlignmentOperation<L, LOp>>>,
            >

    val dagNodeOuterAlignmentCache: MutableMap<
            Pair<AlignableDAGNode<L, LOp, L, LOp>, AlignableDAGNode<L, LOp, L, LOp>>,
            Optional<MetricAlignment<AlignableDAGNode<L, LOp, L, LOp>, AlignableDAGNodeAlignmentOperation<L, LOp, L, LOp>>>,
            >
}