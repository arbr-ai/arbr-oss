package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

sealed interface RealValuedStructTensor<S : Shape.Sum<S0, S1>, S0: Shape, S1: Shape> :
    StructTensor<S, S0, S1, GroundField.Real, Scalar> {

    /**
     * Real-valued Tensor pair
     */
    data class Two<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape>(
        override val t0: T0,
        override val t1: T1,
    ) : StructTensor.Two<T0, S0, T1, S1, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, S1> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Three<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
    ) : StructTensor.Three<T0, S0, T1, S1, T2, S2, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, S2>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Four<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape, T3 : Tensor<S3, GroundField.Real, Scalar>, S3 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
        override val t3: T3,
    ) : StructTensor.Four<T0, S0, T1, S1, T2, S2, T3, S3, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, S3>>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Five<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape, T3 : Tensor<S3, GroundField.Real, Scalar>, S3 : Shape, T4 : Tensor<S4, GroundField.Real, Scalar>, S4 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
        override val t3: T3,
        override val t4: T4,
    ) : StructTensor.Five<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Six<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape, T3 : Tensor<S3, GroundField.Real, Scalar>, S3 : Shape, T4 : Tensor<S4, GroundField.Real, Scalar>, S4 : Shape, T5 : Tensor<S5, GroundField.Real, Scalar>, S5 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
        override val t3: T3,
        override val t4: T4,
        override val t5: T5,
    ) : StructTensor.Six<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Seven<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape, T3 : Tensor<S3, GroundField.Real, Scalar>, S3 : Shape, T4 : Tensor<S4, GroundField.Real, Scalar>, S4 : Shape, T5 : Tensor<S5, GroundField.Real, Scalar>, S5 : Shape, T6 : Tensor<S6, GroundField.Real, Scalar>, S6 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
        override val t3: T3,
        override val t4: T4,
        override val t5: T5,
        override val t6: T6,
    ) : StructTensor.Seven<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
    }

    data class Eight<T0 : Tensor<S0, GroundField.Real, Scalar>, S0 : Shape, T1 : Tensor<S1, GroundField.Real, Scalar>, S1 : Shape, T2 : Tensor<S2, GroundField.Real, Scalar>, S2 : Shape, T3 : Tensor<S3, GroundField.Real, Scalar>, S3 : Shape, T4 : Tensor<S4, GroundField.Real, Scalar>, S4 : Shape, T5 : Tensor<S5, GroundField.Real, Scalar>, S5 : Shape, T6 : Tensor<S6, GroundField.Real, Scalar>, S6 : Shape, T7 : Tensor<S7, GroundField.Real, Scalar>, S7 : Shape>(
        override val t0: T0,
        override val t1: T1,
        override val t2: T2,
        override val t3: T3,
        override val t4: T4,
        override val t5: T5,
        override val t6: T6,
        override val t7: T7,
    ) : StructTensor.Eight<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, T7, S7, GroundField.Real, Scalar> {
        override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>> = Shape.Sum.of(tensor0.shape, tensor1.shape)

    }

}