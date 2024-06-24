package com.arbr.platform.ml.linear.typed.base

import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import org.ejml.simple.SimpleMatrix

interface Matrix<NumRows: Dim, NumCols: Dim>: HomogeneousTensor<Shape.Product<NumRows, NumCols>, NumRows, NumCols> {
    override val order: Int get() = 2

    val numRows: Int
    val numColumns: Int

    val numRowsShape: NumRows
    val numColsShape: NumCols

    override fun dimensionSize(dimensionIndex: Int): Int {
        return when (dimensionIndex) {
            0 -> numRows
            1 -> numColumns
            else -> 0
        }
    }

    operator fun get(i: Int, j: Int): Double

    fun getRow(i: Int): RowVector<NumCols>

    fun getColumn(j: Int): ColumnVector<NumRows>

    fun transpose(): Matrix<NumCols, NumRows>

    fun asSimpleMatrix(): SimpleMatrix

    fun asArray(): Array<DoubleArray>

    /**
     * Scale by a double value
     */
    fun scaleBy(double: Double): Matrix<NumRows, NumCols>

    /**
     * Scale by a scalar
     */
    fun scaleBy(scalar: Scalar): Matrix<NumRows, NumCols>

    /**
     * Multiply matrices
     * (M x N) * (N x P) -> (M x P)
     */
    fun <OtherNumCols: Dim> mult(otherMatrix: Matrix<out NumCols, OtherNumCols>): Matrix<NumRows, OtherNumCols>

    /**
     * Add matrices
     */
    fun plus(otherMatrix: Matrix<NumRows, NumCols>): Matrix<NumRows, NumCols>

    fun concatRows(vararg otherMatrices: Matrix<*, NumCols>): Matrix<Dim.AtLeast<NumRows>, NumCols>

    fun concatColumns(vararg otherMatrices: Matrix<NumRows, *>): Matrix<NumRows, Dim.AtLeast<NumCols>>
}