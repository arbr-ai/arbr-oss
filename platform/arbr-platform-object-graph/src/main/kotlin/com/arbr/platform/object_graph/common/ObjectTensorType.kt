package com.arbr.platform.object_graph.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.ml.linear.typed.tensor.type.TensorType

interface ObjectTensorType<ValueType, S : Shape> :
    TensorType<ValueType, Tensor<S, GroundField.Real, Scalar>, S, GroundField.Real, Scalar> {
    override val tensor: Tensor<S, GroundField.Real, Scalar>
}
