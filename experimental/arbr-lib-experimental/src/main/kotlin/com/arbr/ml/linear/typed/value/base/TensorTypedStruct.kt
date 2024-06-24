package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.StructTensor
import com.arbr.platform.ml.linear.typed.shape.Shape

interface TensorTypedStruct<V, SS : Shape.Sum<SS0, SS1>, SS0 : Shape, SS1 : Shape, F : GroundField<K>, K> :
    TensorTypedValue<V, StructTensor<SS, SS0, SS1, F, K>, SS, F, K> {

    /**
     * The underlying struct value
     */
    override val value: V

    /**
     * The tensor representing the entire struct
     */
    override val tensor: StructTensor<SS, SS0, SS1, F, K>
}