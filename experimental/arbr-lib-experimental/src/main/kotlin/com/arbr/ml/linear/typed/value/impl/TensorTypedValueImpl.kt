package com.arbr.ml.linear.typed.value.impl

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.ml.linear.typed.value.base.TensorTypedValue

class TensorTypedValueImpl<V, T : Tensor<S, F, K>, S : Shape, F: GroundField<K>, K>(
    override val value: V,
    override val tensor: T,
): TensorTypedValue<V, T, S, F, K> {

    override fun toString(): String {
        return super.toString() + "(value=${value}, tensor=${tensor})"
    }
}
