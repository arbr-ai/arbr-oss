package com.arbr.platform.ml.linear.typed.shape

sealed interface Shape {

    /**
     * Note that the singleton Shape is defined as Dim
     */

    /**
     * Sum Type, as in Sigma Type or Tensor Algebra direct sum
     */
    interface Sum<S0: Shape, S1: Shape>: Shape {

        val left: S0
        val right: S1

        companion object {
            private class SumImpl<S0: Shape, S1: Shape>(
                override val left: S0,
                override val right: S1,
            ): Sum<S0, S1>

            fun <S0: Shape, S1: Shape> of(s0: S0, s1: S1): Sum<S0, S1> {
                return SumImpl(s0, s1)
            }
        }
    }

    /**
     * Product Type, as in Pi Type or Tensor Algebra product
     */
    interface Product<S0: Shape, S1: Shape>: Shape {

        val left: S0
        val right: S1

        companion object {
            private class ProductImpl<S0: Shape, S1: Shape>(
                override val left: S0,
                override val right: S1,
            ): Product<S0, S1>

            fun <S0: Shape, S1: Shape> of(s0: S0, s1: S1): Product<S0, S1> {
                return ProductImpl(s0, s1)
            }
        }

    }

}
