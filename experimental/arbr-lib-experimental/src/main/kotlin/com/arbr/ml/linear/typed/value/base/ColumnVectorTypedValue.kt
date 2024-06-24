package com.arbr.ml.linear.typed.value.base

import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

interface ColumnVectorTypedValue<V, Size : Dim> :
    HomogeneousTensorTypedValue<V, ColumnVector<Size>, Shape.Product<Size, Dim.One>, Size, Dim.One>