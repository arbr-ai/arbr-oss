package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.util.collections.mapToArray
import com.arbr.util.invariants.Invariants
import org.ejml.simple.SimpleMatrix

open class TypedColumnVector<Size : Dim>(
    // Size x One matrix
    private val innerMatrix: SimpleMatrix,
    private val sizeShape: Size,
) : TypedMatrix<Size, Dim.One>(
    innerMatrix,
    sizeShape,
    Dim.One,
), ColumnVector<Size> {

    constructor(innerArray: Array<DoubleArray>, sizeShape: Size) : this(SimpleMatrix(innerArray), sizeShape)

    constructor(flatArray: DoubleArray, sizeShape: Size) : this(SimpleMatrix(flatArray.mapToArray { doubleArrayOf(it) }), sizeShape)

    private val innerNumRows = innerMatrix.numRows

    override val size: Int = innerNumRows
    override val numRows: Int = innerNumRows
    override val numColumns: Int = 1

    override operator fun get(i: Int): Double {
        return super.get(i, 0)
    }

    override fun scale(scaleBy: Double): ColumnVector<Size> {
        return TypedColumnVector(innerMatrix.scale(scaleBy), sizeShape)
    }

    override fun tensorGet(vararg indices: Int): Double {
        return get(indices[0])
    }

    override fun filter(selector: (Double) -> Boolean): ColumnVector<Dim.AtMost<Size>> {
        val newValueList = (0 until size)
            .mapNotNull {
                val innerValue = get(it)
                if (selector(innerValue)) {
                    doubleArrayOf(innerValue)
                } else {
                    null
                }
            }

        return TypedColumnVector(
            newValueList.toTypedArray(),
            object : Dim.AtMost<Size> {},
        )
    }

    override fun asFlatArray(): DoubleArray {
        return DoubleArray(size) { get(it) }
    }

    override fun dot(otherVector: ColumnVector<Size>): Scalar {
        Invariants.check {
            require(size == otherVector.size)
        }

        var sum = 0.0
        for (i in 0..<size) {
            sum += this[i] * otherVector[i]
        }

        return TypedScalar(sum)
    }

    override fun hadamard(otherVector: ColumnVector<Size>): ColumnVector<Size> {
        Invariants.check {
            require(size == otherVector.size)
        }

        val product = DoubleArray(size) { i ->
            this[i] * otherVector[i]
        }
        return TypedColumnVector(product, sizeShape)
    }

    override fun plus(otherVector: ColumnVector<Size>): ColumnVector<Size> {
        Invariants.check {
            require(size == otherVector.size)
        }

        val product = DoubleArray(size) { i ->
            this[i] + otherVector[i]
        }
        return TypedColumnVector(product, sizeShape)
    }

    override fun concatRows(vararg otherVectors: ColumnVector<*>): ColumnVector<Dim.AtLeast<Size>> {
        return TypedColumnVector(
            innerMatrix.concatRows(*otherVectors.mapToArray { it.asSimpleMatrix() }),
            object : Dim.AtLeast<Size> {},
        )
    }

    override fun toString(): String {
        return super.toString().replace("Matrix", "Vector")
    }
}