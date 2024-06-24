package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.impl.TypedMatrix
import com.arbr.platform.ml.linear.typed.impl.TypedScalar
import com.arbr.platform.ml.linear.typed.shape.Dim

sealed interface GroundField<K> {
    val zero: K

    data object Real: GroundField<Scalar> {
        override val zero: Scalar = TypedScalar(0.0)
    }
    data object Complex: GroundField<Matrix<Dim.Two, Dim.Two>> {
        override val zero: Matrix<Dim.Two, Dim.Two> = TypedMatrix(
            arrayOf(
                doubleArrayOf(0.0, 0.0),
                doubleArrayOf(0.0, 0.0),
            ),
            Dim.Two,
            Dim.Two,
        )
    }

    // ... Finite fields ...
}
