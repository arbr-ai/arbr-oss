package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.RowVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

interface RowVectorTypedValue<V, Size : Dim> :
    HomogeneousTensorTypedValue<V, RowVector<Size>, Shape.Product<Dim.One, Size>, Dim.One, Size>