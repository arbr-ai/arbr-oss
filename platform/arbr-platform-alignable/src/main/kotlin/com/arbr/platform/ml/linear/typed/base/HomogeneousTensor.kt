package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.ProductTensor

/**
 * A tensor, specifically the Torch restriction to tensors of homogeneous or "rectangular" shape
 * Formally, a product type over a base field.
 */
sealed interface HomogeneousTensor<S : Shape.Product<S0, S1>, S0 : Shape, S1 : Shape> :
    ProductTensor<S, S0, S1, GroundField.Real, Scalar> {

    /**
     * Number of dimensions / index length
     */
    val order: Int

    fun tensorGet(vararg indices: Int): Double

    fun dimensionSize(dimensionIndex: Int): Int

    fun normF(): Double

    fun toDetailedString(): String
}

