package com.arbr.platform.ml.linear.typed.tensor.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.impl.SumTensorImpl

interface SumTensor<S : Shape.Sum<S0, S1>, S0 : Shape, S1 : Shape, F : GroundField<K>, K> : Tensor<S, F, K> {

    /**
     * The left-hand term.
     */
    val tensor0: Tensor<S0, F, K>

    /**
     * The right-hand term.
     */
    val tensor1: Tensor<S1, F, K>

    override val typeTree: TensorTypeTree
        get() = TensorTypeTree.SumOf(
            tensor0.typeTree,
            tensor1.typeTree,
        )

    companion object {
        fun <S0 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
        ): Tensor<S0, F, K> {
            return u0
        }

        fun <S0 : Shape, S1 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
        ): SumTensor<
                Shape.Sum<S0, S1>, S0, S1, F, K
                > {
            return SumTensorImpl(u0, u1, Shape.Sum.of(u0.shape, u1.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, S2>>, S0, Shape.Sum<S1, S2>, F, K
                > {
            val innerSumTensor = of(u1, u2)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, S3>>>, S0, Shape.Sum<S1, Shape.Sum<S2, S3>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4, u5)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, S6 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
            u6: Tensor<S6, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4, u5, u6)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, S6 : Shape, S7 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
            u6: Tensor<S6, F, K>,
            u7: Tensor<S7, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4, u5, u6, u7)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, S6 : Shape, S7 : Shape, S8 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
            u6: Tensor<S6, F, K>,
            u7: Tensor<S7, F, K>,
            u8: Tensor<S8, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, Shape.Sum<S7, S8>>>>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, Shape.Sum<S7, S8>>>>>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4, u5, u6, u7, u8)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, S6 : Shape, S7 : Shape, S8 : Shape, S9 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
            u6: Tensor<S6, F, K>,
            u7: Tensor<S7, F, K>,
            u8: Tensor<S8, F, K>,
            u9: Tensor<S9, F, K>,
        ): SumTensor<
                Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, Shape.Sum<S7, Shape.Sum<S8, S9>>>>>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, Shape.Sum<S7, Shape.Sum<S8, S9>>>>>>>>, F, K
                > {
            val innerSumTensor = of(u1, u2, u3, u4, u5, u6, u7, u8, u9)
            return SumTensorImpl(u0, innerSumTensor, Shape.Sum.of(u0.shape, innerSumTensor.shape))
        }
    }
}
