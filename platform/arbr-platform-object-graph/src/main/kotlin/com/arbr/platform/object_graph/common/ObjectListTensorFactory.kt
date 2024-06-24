package com.arbr.platform.object_graph.common

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.ProductTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

object ObjectListTensorFactory {
    private const val LIST_TYPE_IDENTIFIER = "ListOf"

    private fun <T: Tensor<S, GroundField.Real, Scalar>, S: Shape> reduceListWithPositionalEncoding(
        tensors: List<T>,
    ): Tensor<S, GroundField.Real, Scalar> {
        // TODO: Implement
        return tensors.firstOrNull() ?: Tensor.zero()
    }

    fun <T: Tensor<S, GroundField.Real, Scalar>, S: Shape> of(
        t0: T,
    ): ProductTensor<Shape.Product<Dim.VariableC, S>, Dim.VariableC, S, GroundField.Real, Scalar> {
        return ProductTensor.of(
            SingletonTensorImpl(LIST_TYPE_IDENTIFIER, Dim.VariableC),
            t0,
        )
    }

    fun <T: Tensor<S, GroundField.Real, Scalar>, S: Shape> ofList(
        innerTensors: List<T>,
    ): ProductTensor<Shape.Product<Dim.VariableC, S>, Dim.VariableC, S, GroundField.Real, Scalar> {
        return ProductTensor.of(
            SingletonTensorImpl(LIST_TYPE_IDENTIFIER, Dim.VariableC),
            reduceListWithPositionalEncoding(innerTensors),
        )
    }
}
