package com.arbr.platform.ml.linear.typed.tensor.base

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.base.GroundField

/**
 * A Tensor of a singleton dimension, i.e. a plain vector space.
 */
interface SingletonTensor<D: Dim, F: GroundField<K>, K>: Tensor<D, F, K> {

    /**
     * Base tensor type identifier for the leaf type.
     */
    val tensorTypeIdentifier: String

    override val typeTree: TensorTypeTree
        get() = TensorTypeTree.Leaf(tensorTypeIdentifier)
}
