package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

interface ColumnVector<Size : Dim> : Matrix<Size, Dim.One>,
    Vector<ColumnVector<Size>, Shape.Product<Size, Dim.One>, Size, Dim.One, Size> {
    override val order: Int get() = 1

    override fun dimensionSize(dimensionIndex: Int): Int {
        return when (dimensionIndex) {
            0 -> size
            else -> 0
        }
    }

    fun scale(scaleBy: Double): ColumnVector<Size>

    fun filter(selector: (Double) -> Boolean): ColumnVector<Dim.AtMost<Size>>

    fun concatRows(vararg otherVectors: ColumnVector<*>): ColumnVector<Dim.AtLeast<Size>>
}