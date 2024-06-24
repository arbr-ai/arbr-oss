package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.ObjectTensor
import com.arbr.platform.ml.linear.typed.shape.Shape

interface TensorTypedObject<V, S0 : Shape, SS : Shape.Sum<SS0, SS1>, SS0 : Shape, SS1 : Shape, F : GroundField<K>, K> :
    TensorTypedValue<V, ObjectTensor<S0, SS, SS0, SS1, F, K>, Shape.Product<S0, SS>, F, K> {

    /**
     * The typed struct value which this object wraps.
     */
    val tensorTypedStruct: TensorTypedStruct<V, SS, SS0, SS1, F, K>

    /**
     * The underlying value of the object
     */
    override val value: V

    /**
     * The outer tensor encoding the object's type and acting as a quotient across the Struct sum tensor
     */
    override val tensor: ObjectTensor<S0, SS, SS0, SS1, F, K>
}
