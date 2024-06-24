package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.HomogeneousTensor
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Shape

interface HomogeneousTensorTypedValue<V, T : HomogeneousTensor<S, S0, S1>, S : Shape.Product<S0, S1>, S0: Shape, S1: Shape> :
    TensorTypedValue<V, T, S, GroundField.Real, Scalar>