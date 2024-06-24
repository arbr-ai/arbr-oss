package com.arbr.platform.alignable.alignable.v2.dag

import com.arbr.platform.alignable.alignable.MetricAlignable

/**
 * Type wrapper for simplifying implementation signatures.
 */
interface ConstructiveDAGStateInducer<Q : Any, O : MetricAlignable<O, O2>, O2: Any> :
    ConstructiveStateInducer<Q, AlignableDAGNode<O, O2, O, O2>, AlignableDAGNodeAlignmentOperation<O, O2, O, O2>>