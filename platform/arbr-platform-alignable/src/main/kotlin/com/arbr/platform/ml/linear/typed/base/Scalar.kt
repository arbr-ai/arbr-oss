package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim

interface Scalar: ColumnVector<Dim.One> {
    override val order: Int get() = 0

    override val size: Int get() = 1

    val value: Double
}
