package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.base.RowVector
import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.base.Matrix
import com.arbr.platform.ml.linear.typed.base.Vector

fun Matrix<Dim.One, Dim.One>.asScalar(): Scalar {
    return TypedScalar(get(0, 0))
}

fun <Size: Dim> Matrix<Size, Dim.One>.asColumnVector(): ColumnVector<Size> {
    return TypedColumnVector(asSimpleMatrix(), numRowsShape)
}

fun <Size: Dim> Matrix<Dim.One, Size>.asRowVector(): RowVector<Size> {
    return TypedRowVector(asSimpleMatrix(), numColsShape)
}

fun Vector<*, *, *, *, Dim.One>.asScalar(): Scalar {
    return TypedScalar(get(0))
}

class TypeConversions {
}