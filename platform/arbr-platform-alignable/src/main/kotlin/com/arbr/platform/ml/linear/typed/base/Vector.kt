package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

sealed interface Vector<V: Vector<V, S, S0, S1, Size>, S : Shape.Product<S0, S1>, S0 : Shape, S1 : Shape, Size: Dim>:
    HomogeneousTensor<S, S0, S1> {
    override val order: Int get() = 1

    val size: Int

    override fun dimensionSize(dimensionIndex: Int): Int {
        return when (dimensionIndex) {
            0 -> size
            else -> 0
        }
    }

    operator fun get(i: Int): Double

    fun asFlatArray(): DoubleArray

    fun dot(otherVector: V): Scalar

    /**
     * Compute the hadamard (elementwise) product.
     */
    fun hadamard(otherVector: V): V

    /**
     * Compute the vector sum.
     */
    fun plus(otherVector: V): V
}