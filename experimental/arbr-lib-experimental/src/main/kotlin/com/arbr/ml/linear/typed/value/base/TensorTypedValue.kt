package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.ml.linear.typed.value.impl.TensorTypedValueImpl

interface TensorTypedValue<V, T : Tensor<S, F, K>, S : Shape, F : GroundField<K>, K> {
    val value: V
    val tensor: T

    fun <TTV : TensorTypedValue<V1, T1, S1, F, K>, V1, T1 : Tensor<S1, F, K>, S1 : Shape, V2, T2 : Tensor<S2, F, K>, S2 : Shape> combineWith(
        other: TTV,
        combine: (Pair<V, T>, Pair<V1, T1>) -> Pair<V2, T2>,
    ): TensorTypedValue<V2, T2, S2, F, K> {
        val (newValue, newTensor) = combine(value to tensor, other.value to other.tensor)
        return TensorTypedValueImpl(newValue, newTensor)
    }
}

