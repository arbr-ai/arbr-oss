package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.base.Matrix
import com.arbr.platform.ml.linear.typed.base.RowVector
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.util.invariants.Invariants
import com.arbr.util.collections.mapToArray
import org.ejml.simple.SimpleMatrix

open class TypedRowVector<Size : Dim>(
    // One x Size matrix
    private val innerMatrix: SimpleMatrix,
    private val sizeShape: Size,
) : TypedMatrix<Dim.One, Size>(
    innerMatrix,
    Dim.One,
    sizeShape,
), RowVector<Size> {

    constructor(innerArray: Array<DoubleArray>, sizeShape: Size) : this(SimpleMatrix(innerArray), sizeShape)

    constructor(flatArray: DoubleArray, sizeShape: Size) : this(SimpleMatrix(arrayOf(flatArray)), sizeShape)

    private val innerNumCols = innerMatrix.numCols

    override val size: Int = innerNumCols
    override val numRows: Int = 1
    override val numColumns: Int = innerNumCols

    override fun scale(scaleBy: Double): RowVector<Size> {
        return TypedRowVector(innerMatrix.scale(scaleBy), sizeShape)
    }

    override operator fun get(i: Int): Double {
        return super.get(0, i)
    }

    override fun tensorGet(vararg indices: Int): Double {
        return get(indices[0])
    }

    override fun filter(selector: (Double) -> Boolean): RowVector<Dim.AtMost<Size>> {
        val newValueList = (0 until size)
            .mapNotNull {
                val innerValue = get(it)
                if (selector(innerValue)) {
                    innerValue
                } else {
                    null
                }
            }

        return TypedRowVector(
            arrayOf(newValueList.toDoubleArray()),
            object : Dim.AtMost<Size> {},
        )
    }

    override fun asFlatArray(): DoubleArray {
        return innerMatrix.toArray2().firstOrNull() ?: doubleArrayOf()
    }

    override fun dot(otherVector: RowVector<Size>): Scalar {
        Invariants.check {
            require(size == otherVector.size)
        }

        var sum = 0.0
        for (i in 0..<size) {
            sum += this[i] * otherVector[i]
        }

        return TypedScalar(sum)
    }

    override fun hadamard(otherVector: RowVector<Size>): RowVector<Size> {
        Invariants.check {
            require(size == otherVector.size)
        }

        val product = DoubleArray(size) { i ->
            this[i] * otherVector[i]
        }
        return TypedRowVector(product, sizeShape)
    }

    override fun plus(otherVector: RowVector<Size>): RowVector<Size> {
        Invariants.check {
            require(size == otherVector.size)
        }

        val product = DoubleArray(size) { i ->
            this[i] + otherVector[i]
        }
        return TypedRowVector(product, sizeShape)
    }

    override fun <NumRows : Dim> concatRows(
        numRowsShape: NumRows,
        vararg otherRows: RowVector<Size>
    ): Matrix<NumRows, Size> {
        return TypedMatrix(
            innerMatrix.concatRows(*otherRows.mapToArray { it.asSimpleMatrix() }),
            numRowsShape,
            sizeShape,
        )
    }

    override fun toString(): String {
        return super.toString().replace("Matrix", "Vector")
    }
}