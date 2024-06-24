package com.arbr.platform.object_graph.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

data class ObjectTensorTypeImpl<ValueType, S : Shape>(
    override val tensor: Tensor<S, GroundField.Real, Scalar>,
) : ObjectTensorType<ValueType, S> {

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun <ValueType, S : Shape> forValueClass(
            valueClass: Class<ValueType>,
            tensor: Tensor<S, GroundField.Real, Scalar>
        ): ObjectTensorTypeImpl<ValueType, S> {
            return ObjectTensorTypeImpl(tensor)
        }
    }
}
