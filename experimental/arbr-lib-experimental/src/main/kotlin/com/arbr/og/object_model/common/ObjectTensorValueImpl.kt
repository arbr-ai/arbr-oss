package com.arbr.og.object_model.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

class ObjectTensorValueImpl<ValueType, ST : Shape, SF: Shape>(
    override val tensorType: ObjectTensorType<ValueType, ST>,
    override val tensor: Tensor<Shape.Product<ST, SF>, GroundField.Real, Scalar>,
    override val value: ValueType,
): ObjectTensorValue<ValueType, ST, SF>