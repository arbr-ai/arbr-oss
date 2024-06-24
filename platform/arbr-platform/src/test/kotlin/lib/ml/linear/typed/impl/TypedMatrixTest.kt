package com.arbr.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.impl.TypedMatrix
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.junit.jupiter.api.Test

class TypedMatrixTest {

    private val matrix0 = TypedMatrix<Dim.VariableM, Dim.VariableN>(
        arrayOf(
            doubleArrayOf(1.0, 1.0, 0.0),
            doubleArrayOf(0.0, 1.0, 1.0),
            doubleArrayOf(0.1, 1.0, 0.31),
            doubleArrayOf(1.0, 0.2, 0.0),
        ),
        Dim.VariableM,
        Dim.VariableN,
    )
    private val matrix1 = TypedMatrix<Dim.VariableM, Dim.VariableN>(
        arrayOf(
            doubleArrayOf(1.0, 0.0, 1.0),
            doubleArrayOf(1.0, 1.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.5),
            doubleArrayOf(1.0, -0.2, 0.0),
        ),
        Dim.VariableM,
        Dim.VariableN,
    )

    @Test
    fun `adds matrices`() {
        println((matrix0.plus(matrix1)).toDetailedString())
    }

    @Test
    fun `multiplies matrices`() {
        println(matrix0.transpose().mult(matrix1).toDetailedString())
    }

}