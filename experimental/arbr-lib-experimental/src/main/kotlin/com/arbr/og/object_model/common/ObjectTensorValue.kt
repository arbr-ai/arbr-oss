package com.arbr.og.object_model.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.ml.linear.typed.tensor.type.TensorType

interface ObjectTensorValue<ValueType, ST : Shape, SF : Shape> :
    TensorType<ValueType, Tensor<Shape.Product<ST, SF>, GroundField.Real, Scalar>, Shape.Product<ST, SF>, GroundField.Real, Scalar> {
    val tensorType: ObjectTensorType<ValueType, ST>
    val value: ValueType
}

