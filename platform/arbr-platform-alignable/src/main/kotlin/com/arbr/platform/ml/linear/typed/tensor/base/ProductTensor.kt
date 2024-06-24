
package com.arbr.platform.ml.linear.typed.tensor.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.impl.ProductTensorImpl

interface ProductTensor<S : Shape.Product<S0, S1>, S0 : Shape, S1 : Shape, F : GroundField<K>, K> : Tensor<S, F, K> {

    /**
     * The left-hand factor.
     */
    val tensor0: Tensor<S0, F, K>

    /**
     * The right-hand factor.
     */
    val tensor1: Tensor<S1, F, K>

    override val typeTree: TensorTypeTree
        get() = TensorTypeTree.ProductOf(
            tensor0.typeTree,
            tensor1.typeTree,
        )

    companion object {
        fun <S0 : Shape, S1 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>
        ): ProductTensor<
                Shape.Product<S0, S1>, S0, S1, F, K
                > {
            return ProductTensorImpl(u0, u1, Shape.Product.of(u0.shape, u1.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, S2>>, S0, Shape.Product<S1, S2>, F, K
                > {
            val innerProductTensor = of(u1, u2)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, S3>>>, S0, Shape.Product<S1, Shape.Product<S2, S3>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, S4>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, S4>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, S5>>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, S5>>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4, u5)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
        }

        fun <S0 : Shape, S1 : Shape, S2 : Shape, S3 : Shape, S4 : Shape, S5 : Shape, S6 : Shape, F : GroundField<K>, K> of(
            u0: Tensor<S0, F, K>,
            u1: Tensor<S1, F, K>,
            u2: Tensor<S2, F, K>,
            u3: Tensor<S3, F, K>,
            u4: Tensor<S4, F, K>,
            u5: Tensor<S5, F, K>,
            u6: Tensor<S6, F, K>,
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, S6>>>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, S6>>>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4, u5, u6)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
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
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, S7>>>>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, S7>>>>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4, u5, u6, u7)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
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
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, Shape.Product<S7, S8>>>>>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, Shape.Product<S7, S8>>>>>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4, u5, u6, u7, u8)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
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
        ): ProductTensor<
                Shape.Product<S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, Shape.Product<S7, Shape.Product<S8, S9>>>>>>>>>, S0, Shape.Product<S1, Shape.Product<S2, Shape.Product<S3, Shape.Product<S4, Shape.Product<S5, Shape.Product<S6, Shape.Product<S7, Shape.Product<S8, S9>>>>>>>>, F, K
                > {
            val innerProductTensor = of(u1, u2, u3, u4, u5, u6, u7, u8, u9)
            return ProductTensorImpl(u0, innerProductTensor, Shape.Product.of(u0.shape, innerProductTensor.shape))
        }
    }
}
