package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.SumTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

sealed interface StructTensor<S : Shape.Sum<S0, S1>, S0 : Shape, S1 : Shape, F : GroundField<K>, K> :
    SumTensor<S, S0, S1, F, K> {

    /**
     * Tensor pair
     */
    interface Two<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, S1>, S0, S1, F, K> {
        val t0: T0
        val t1: T1

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<S1, F, K> get() = t1

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
            ): Two<T0, S0, T1, S1, F, K> {
                return object : Two<T0, S0, T1, S1, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val shape: Shape.Sum<S0, S1> = Shape.Sum.of(t0.shape, t1.shape)
                }
            }
        }
    }

    interface Three<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, S2>>, S0, Shape.Sum<S1, S2>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, S2>, F, K> get() = Two.of(t1, t2)

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
            ): Three<T0, S0, T1, S1, T2, S2, F, K> {
                return object : Three<T0, S0, T1, S1, T2, S2, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, S2>> = Shape.Sum.of(tensor0.shape, tensor1.shape)
                }
            }
        }
    }

    interface Four<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, S3>>>, S0, Shape.Sum<S1, Shape.Sum<S2, S3>>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2
        val t3: T3

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, Shape.Sum<S2, S3>>, F, K> get() = Three.of(t1, t2, t3)

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
                u3: T3,
            ): Four<T0, S0, T1, S1, T2, S2, T3, S3, F, K> {
                return object : Four<T0, S0, T1, S1, T2, S2, T3, S3, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val t3: T3 = u3
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, S3>>> =
                        Shape.Sum.of(tensor0.shape, tensor1.shape)
                }
            }
        }
    }

    interface Five<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2
        val t3: T3
        val t4: T4

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>, F, K>
            get() = Four.of(
                t1,
                t2,
                t3,
                t4
            )

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
                u3: T3,
                u4: T4,
            ): Five<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, F, K> {
                return object : Five<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val t3: T3 = u3
                    override val t4: T4 = u4
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, S4>>>> =
                        Shape.Sum.of(tensor0.shape, tensor1.shape)
                }
            }
        }
    }

    interface Six<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2
        val t3: T3
        val t4: T4
        val t5: T5

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>, F, K>
            get() = Five.of(
                t1,
                t2,
                t3,
                t4,
                t5
            )

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
                u3: T3,
                u4: T4,
                u5: T5,
            ): Six<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, F, K> {
                return object : Six<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val t3: T3 = u3
                    override val t4: T4 = u4
                    override val t5: T5 = u5
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, S5>>>>> =
                        Shape.Sum.of(tensor0.shape, tensor1.shape)
                }
            }

        }
    }

    interface Seven<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, T6 : Tensor<S6, F, K>, S6 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>>, S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2
        val t3: T3
        val t4: T4
        val t5: T5
        val t6: T6

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>, F, K>
            get() = Six.of(
                t1,
                t2,
                t3,
                t4,
                t5,
                t6
            )

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, T6 : Tensor<S6, F, K>, S6 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
                u3: T3,
                u4: T4,
                u5: T5,
                u6: T6,
            ): Seven<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, F, K> {
                return object : Seven<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val t3: T3 = u3
                    override val t4: T4 = u4
                    override val t5: T5 = u5
                    override val t6: T6 = u6
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, S6>>>>>> =
                        Shape.Sum.of(tensor0.shape, tensor1.shape)
                }
            }

        }
    }

    interface Eight<T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, T6 : Tensor<S6, F, K>, S6 : Shape, T7 : Tensor<S7, F, K>, S7 : Shape, F : GroundField<K>, K> :
        StructTensor<Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>>,
                S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>, F, K> {
        val t0: T0
        val t1: T1
        val t2: T2
        val t3: T3
        val t4: T4
        val t5: T5
        val t6: T6
        val t7: T7

        override val tensor0: Tensor<S0, F, K> get() = t0
        override val tensor1: Tensor<Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>, F, K>
            get() = Seven.of(
                t1,
                t2,
                t3,
                t4,
                t5,
                t6,
                t7
            )

        companion object {
            fun <T0 : Tensor<S0, F, K>, S0 : Shape, T1 : Tensor<S1, F, K>, S1 : Shape, T2 : Tensor<S2, F, K>, S2 : Shape, T3 : Tensor<S3, F, K>, S3 : Shape, T4 : Tensor<S4, F, K>, S4 : Shape, T5 : Tensor<S5, F, K>, S5 : Shape, T6 : Tensor<S6, F, K>, S6 : Shape, T7 : Tensor<S7, F, K>, S7 : Shape, F : GroundField<K>, K> of(
                u0: T0,
                u1: T1,
                u2: T2,
                u3: T3,
                u4: T4,
                u5: T5,
                u6: T6,
                u7: T7,
            ): Eight<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, T7, S7, F, K> {
                return object : Eight<T0, S0, T1, S1, T2, S2, T3, S3, T4, S4, T5, S5, T6, S6, T7, S7, F, K> {
                    override val t0: T0 = u0
                    override val t1: T1 = u1
                    override val t2: T2 = u2
                    override val t3: T3 = u3
                    override val t4: T4 = u4
                    override val t5: T5 = u5
                    override val t6: T6 = u6
                    override val t7: T7 = u7
                    override val shape: Shape.Sum<S0, Shape.Sum<S1, Shape.Sum<S2, Shape.Sum<S3, Shape.Sum<S4, Shape.Sum<S5, Shape.Sum<S6, S7>>>>>>> =
                        Shape.Sum.of(tensor0.shape, tensor1.shape)

                }
            }
        }
    }


}