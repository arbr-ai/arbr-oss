package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.ProductTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

class ObjectTensor<S0: Shape, SS: Shape.Sum<SS0, SS1>, SS0: Shape, SS1: Shape, F : GroundField<K>, K>(
    /**
     * The outer tensor encoding the object's type and acting as a quotient across the Struct sum tensor
     */
    override val tensor0: Tensor<S0, F, K>,
    override val tensor1: StructTensor<SS, SS0, SS1, F, K>,
): ProductTensor<Shape.Product<S0, SS>, S0, SS, F, K> {
    override val shape: Shape.Product<S0, SS> = Shape.Product.of(tensor0.shape, tensor1.shape)
}
