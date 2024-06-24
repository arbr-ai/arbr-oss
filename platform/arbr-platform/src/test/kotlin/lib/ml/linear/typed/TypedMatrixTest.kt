package com.arbr.ml.linear.typed

import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TypedMatrixTest {

    @Test
    fun `multiplies square matrices`() {
        val mat0 = TypedMatrices.of<Dim.VariableN, Dim.VariableN>(
            Dim.VariableN,
            Dim.VariableN,
        ) {
            r(1.0, 1.0, 0.1)
            r(0.1, 1.0, 0.2)
            r(0.0, 0.0, 0.3)
        }
        val sm0 = mat0.asSimpleMatrix()

        val mat1 = TypedMatrices.of<Dim.VariableN, Dim.VariableN>(
            Dim.VariableN,
            Dim.VariableN,
        ) {
            r(1.0, -1.0, 0.1)
            r(0.1, 0.0, 0.4)
            r(1.0, 0.0, 0.3)
        }
        val sm1 = mat1.asSimpleMatrix()

        val baseMult = sm0.mult(sm1)

        val product = mat0.mult(mat1)
        println(product.toDetailedString())

        assertEquals(baseMult.numRows, product.numRows)
        assertEquals(baseMult.numCols, product.numColumns)

        val epsilon = 1E-8
        for (i in 0 until baseMult.numRows) {
            for (j in 0 until baseMult.numCols) {
                assertEquals(baseMult.get(i, j), product[i, j], epsilon)
            }
        }
    }

    @Test
    fun `multiplies different matrices`() {
        val mat0 = TypedMatrices.of<Dim.VariableM, Dim.VariableN>(
            Dim.VariableM,
            Dim.VariableN,
        ) {
            r(1.0, 1.0, 0.1)
            r(0.1, 1.0, 0.2)
            r(0.0, 0.0, 0.3)
            r(0.0, 0.0, 0.3)
        }
        val sm0 = mat0.asSimpleMatrix()

        val mat1 = TypedMatrices.of<Dim.VariableN, Dim.VariableP>(
            Dim.VariableN,
            Dim.VariableP,
        ) {
            r(1.0, -1.0, 0.1, 0.1)
            r(0.1, 0.0, 0.4, 0.1)
            r(1.0, 0.0, 0.3, 0.1)
        }
        val sm1 = mat1.asSimpleMatrix()

        val baseMult = sm0.mult(sm1)

        val product = mat0.mult(mat1)
        println(product.toDetailedString())

        assertEquals(baseMult.numRows, product.numRows)
        assertEquals(baseMult.numCols, product.numColumns)

        val epsilon = 1E-8
        for (i in 0 until baseMult.numRows) {
            for (j in 0 until baseMult.numCols) {
                assertEquals(baseMult.get(i, j), product[i, j], epsilon)
            }
        }
    }

}