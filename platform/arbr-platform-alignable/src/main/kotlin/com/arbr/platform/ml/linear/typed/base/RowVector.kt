package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape

interface RowVector<Size : Dim> : Matrix<Dim.One, Size>,
    Vector<RowVector<Size>, Shape.Product<Dim.One, Size>, Dim.One, Size, Size> {
    override val order: Int get() = 1

    override val numRows: Int get() = 1
    override val numColumns: Int get() = size

    override fun dimensionSize(dimensionIndex: Int): Int {
        return when (dimensionIndex) {
            0 -> size
            else -> 0
        }
    }

    fun scale(scaleBy: Double): RowVector<Size>

    fun filter(selector: (Double) -> Boolean): RowVector<Dim.AtMost<Size>>

    fun <NumRows : Dim> concatRows(
        numRowsShape: NumRows,
        vararg otherRows: RowVector<Size>,
    ): Matrix<NumRows, Size>
}
