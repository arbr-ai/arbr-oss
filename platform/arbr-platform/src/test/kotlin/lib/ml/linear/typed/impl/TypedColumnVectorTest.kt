package com.arbr.ml.linear.typed.impl

import com.arbr.platform.ml.linear.typed.impl.TypedColumnVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TypedColumnVectorTest {

    @Test
    fun `computes dot`() {

        val vec0 = TypedColumnVector<Dim.VariableN>(doubleArrayOf(1.0, 0.0, Double.MAX_VALUE, Double.MIN_VALUE), Dim.VariableN)
        val vec1 = TypedColumnVector<Dim.VariableN>(doubleArrayOf(-50.0, -1.0, Double.MIN_VALUE, 0.0), Dim.VariableN)

        assertEquals(Double.MAX_VALUE, vec0.dot(vec1).value)
    }

}
