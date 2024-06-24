package com.arbr.platform.ml.linear.typed.tensor.type

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

/**
 * A value type backed by a tensor but not yet necessarily bound to a concrete value.
 */
interface TensorType<V, T : Tensor<S, F, K>, S : Shape, F : GroundField<K>, K> {
    val tensor: T

    companion object {
        fun <V, T : Tensor<S, F, K>, S : Shape, F : GroundField<K>, K> of(
            tensor: T
        ): TensorType<V, T, S, F, K> {
            return TensorTypeImpl(tensor)
        }
    }
}