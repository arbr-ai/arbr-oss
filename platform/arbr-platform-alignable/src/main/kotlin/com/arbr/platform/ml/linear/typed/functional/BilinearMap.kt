package com.arbr.platform.ml.linear.typed.functional

import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

/**
 * A bilinear map, i.e. a linear map between tensor spaces T and U, therefore typed T -> U functionally
 * Or by an alternate interpretation, (T x U) -> F for the ground field F
 * Also U -> T by right-application
 */
interface BilinearMap<T : Tensor<ST, F, K>, ST : Shape, U : Tensor<SU, F, K>, SU : Shape, F : GroundField<K>, K> {

    /**
     * Apply by left-dot product t* @ B
     * Q: Should we take the conjugate transpose by default?
     */
    fun apply(t: T): U

    /**
     * Apply by right-dot product B @ u
     */
    fun applyRight(u: U): T

    /**
     * Apply by two-sided dot product t* @ B @ u
     */
    fun applyBilinear(t: T, u: U): K

    companion object {

        fun <T : Tensor<ST, F, K>, ST : Shape, F : GroundField<K>, K> identity(): BilinearMap<T, ST, T, ST, F, K> =
            object : BilinearMap<T, ST, T, ST, F, K> {
                override fun apply(t: T): T = t
                override fun applyRight(u: T): T = u
                override fun applyBilinear(t: T, u: T): K = TODO()
            }

    }

}
