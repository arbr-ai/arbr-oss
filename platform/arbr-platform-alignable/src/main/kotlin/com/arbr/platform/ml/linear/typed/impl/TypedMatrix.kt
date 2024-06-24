package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.base.*
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.SingletonTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import org.ejml.simple.SimpleMatrix

open class TypedMatrix<NumRows : Dim, NumCols : Dim>(
    private val innerMatrix: SimpleMatrix,
    final override val numRowsShape: NumRows,
    final override val numColsShape: NumCols,
) : Matrix<NumRows, NumCols> {

    constructor(
        innerArray: Array<DoubleArray>,
        numRowsShape: NumRows,
        numColsShape: NumCols,
    ) : this(
        SimpleMatrix(innerArray),
        numRowsShape,
        numColsShape,
    )

    override val shape: Shape.Product<NumRows, NumCols> = Shape.Product.of(numRowsShape, numColsShape)
    override val numRows: Int = innerMatrix.numRows
    override val numColumns: Int = innerMatrix.numCols

    /**
     * Dimensional tensor representing sample space
     * Not necessarily representable as a concrete object, for example not the same as row vectors
     * The matrix is an element of the sample space restricted by the feature space, or vice versa
     * Without explicit constructions in terms of those component spaces, we can't recover them
     *
     * A concrete pair of Dim objects would be sufficient, but ideally we would like to not require them in every
     * constructor.
     */
    override val tensor0: Tensor<NumRows, GroundField.Real, Scalar>
        get() = object : SingletonTensor<NumRows, GroundField.Real, Scalar> {
            override val shape: NumRows get() = numRowsShape

            override val tensorTypeIdentifier: String get() = throw NotImplementedError("Fabricate this somehow")
        }

    /**
     * Dimensional tensor representing feature space
     */
    override val tensor1: Tensor<NumCols, GroundField.Real, Scalar>
        get() = object : SingletonTensor<NumCols, GroundField.Real, Scalar> {
            override val shape: NumCols get() = numColsShape

            override val tensorTypeIdentifier: String get() = throw NotImplementedError("Fabricate this somehow")
        }

    override operator fun get(i: Int, j: Int): Double {
        return innerMatrix[i, j]
    }

    override fun getRow(i: Int): RowVector<NumCols> {
        return TypedRowVector(innerMatrix.getRow(i), numColsShape)
    }

    override fun getColumn(j: Int): ColumnVector<NumRows> {
        return TypedColumnVector(innerMatrix.getColumn(j), numRowsShape)
    }

    override fun transpose(): Matrix<NumCols, NumRows> {
        return TypedMatrix(
            innerMatrix.transpose(),
            numColsShape,
            numRowsShape,
        )
    }

    override fun asSimpleMatrix(): SimpleMatrix {
        return innerMatrix
    }

    override fun asArray(): Array<DoubleArray> {
        return innerMatrix.toArray2()
    }

    override fun concatColumns(vararg otherMatrices: Matrix<NumRows, *>): Matrix<NumRows, Dim.AtLeast<NumCols>> {
        return TypedMatrix(
            innerMatrix.concatColumns(*otherMatrices.map { it.asSimpleMatrix() }.toTypedArray()),
            numRowsShape,
            object : Dim.AtLeast<NumCols> {},
        )
    }

    override fun concatRows(vararg otherMatrices: Matrix<*, NumCols>): Matrix<Dim.AtLeast<NumRows>, NumCols> {
        return TypedMatrix(
            innerMatrix.concatRows(*otherMatrices.map { it.asSimpleMatrix() }.toTypedArray()),
            object : Dim.AtLeast<NumRows> {},
            numColsShape,
        )
    }

    override fun tensorGet(vararg indices: Int): Double {
        return get(indices[0], indices[1])
    }

    override fun normF(): Double {
        return innerMatrix.normF()
    }

    override fun scaleBy(double: Double): Matrix<NumRows, NumCols> {
        return TypedMatrix(
            innerMatrix.scale(double),
            numRowsShape,
            numColsShape,
        )
    }

    override fun scaleBy(scalar: Scalar): Matrix<NumRows, NumCols> {
        return scaleBy(scalar.value)
    }

    override fun <OtherNumCols : Dim> mult(otherMatrix: Matrix<out NumCols, OtherNumCols>): Matrix<NumRows, OtherNumCols> {
        return TypedMatrix(innerMatrix.mult(otherMatrix.asSimpleMatrix()), numRowsShape, otherMatrix.numColsShape)
    }

    override fun plus(otherMatrix: Matrix<NumRows, NumCols>): Matrix<NumRows, NumCols> {
        return TypedMatrix(innerMatrix.plus(otherMatrix.asSimpleMatrix()), numRowsShape, numColsShape)
    }

    override fun toString(): String {
        val superString = super.toString()
        val atIndex = superString.indexOfLast { it == '@' }
        val suffix = if (atIndex == -1) {
            ""
        } else {
            superString.drop(atIndex)
        }
        return "Matrix[${numRows} x ${numColumns}]" + suffix
    }

    override fun toDetailedString(): String {
        val paddedWidth = 8

        val header = toString()
        return StringBuilder().run {
            append(header, "\n")
            for (i in 0..<numRows) {
                append("|")
                for (j in 0..<numColumns) {
                    val doubleStr = String.format("%.04f", get(i, j))
                    append(" ".repeat(kotlin.math.max(1, paddedWidth - doubleStr.length)))
                    append(doubleStr)
                }
                append(" |")
                if (i < (numRows - 1)) {
                    append("\n")
                }
            }

            toString()
        }
    }

    companion object {
        fun <M : Dim, N : Dim> empty(m: Int, n: Int, shapeM: M, shapeN: N): TypedMatrix<M, N> {
            return TypedMatrix(
                SimpleMatrix(m, n),
                shapeM,
                shapeN,
            )
        }
    }
}