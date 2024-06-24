package com.arbr.platform.ml.linear.typed.functional

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor

/**
 * The most general kind of function across tensor types.
 */
fun interface TensorFunction<T: Tensor<ST, F, K>, ST: Shape, U: Tensor<SU, F, K>, SU: Shape, F: GroundField<K>, K> {

    fun apply(t: T): U

}
