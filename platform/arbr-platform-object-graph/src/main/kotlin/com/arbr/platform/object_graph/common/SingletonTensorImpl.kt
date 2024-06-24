package com.arbr.platform.object_graph.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.tensor.base.SingletonTensor

data class SingletonTensorImpl<S: Dim, F: GroundField<K>, K>(
    private val typeIdentifier: String,
    override val shape: S,
) : SingletonTensor<S, F, K> {
    override val tensorTypeIdentifier: String get() = typeIdentifier
}