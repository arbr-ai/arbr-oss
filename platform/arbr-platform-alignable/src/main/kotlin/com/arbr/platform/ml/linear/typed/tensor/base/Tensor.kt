package com.arbr.platform.ml.linear.typed.tensor.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

/**
 * A tensor in the general algebraic sense, including both homogenous tensors over fields (pure products, commonly
 * conflated with tensors), and heterogeneous tensors
 */
sealed interface Tensor<S : Shape, F : GroundField<K>, K> {

    // TODO: Decide if needed
    val shape: S

    /**
     * Get a base field value by a flat index.
     * TODO: Decide if needed
     */
    // fun getValue(i: Int): K

    val typeTree: TensorTypeTree

    // fun scaleBy(scalar: K): Tensor<S, F, K>

    companion object {
        // TODO: Implement
        fun <S : Shape, F : GroundField<K>, K> zero(): Tensor<S, F, K> {
            @Suppress("UNCHECKED_CAST")
            return object : SingletonTensor<Dim.VariableF, F, K> {
                override val tensorTypeIdentifier: String
                    get() = "zero"
                override val shape: Dim.VariableF = Dim.VariableF
            } as Tensor<S, F, K>
        }

        fun <S0 : Shape, S1 : Shape, F : GroundField<K>, K> product(
            t0: Tensor<S0, F, K>,
            t1: Tensor<S1, F, K>
        ): ProductTensor<Shape.Product<S0, S1>, S0, S1, F, K> {
            return ProductTensor.of(t0, t1)
        }

        fun <S0 : Shape, S1 : Shape, F : GroundField<K>, K> sum(
            t0: Tensor<S0, F, K>,
            t1: Tensor<S1, F, K>
        ): SumTensor<Shape.Sum<S0, S1>, S0, S1, F, K> {
            return SumTensor.of(t0, t1)
        }
    }
}
