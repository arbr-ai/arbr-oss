package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.impl.TypedColumnVector
import org.ejml.simple.SimpleMatrix

data class TypedScalar(
    override val value: Double,
): TypedColumnVector<Dim.One>(
    SimpleMatrix(arrayOf(doubleArrayOf(value))),
    Dim.One,
), Scalar {
    override val order: Int get() = 0

    override val size: Int = 1

    override fun tensorGet(vararg indices: Int): Double {
        return value
    }
}
