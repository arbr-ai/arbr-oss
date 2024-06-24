package com.arbr.ml.linear.typed.value

import com.arbr.platform.ml.linear.typed.impl.TypedRowVector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.value.impl.F
import org.junit.jupiter.api.Test
import kotlin.random.Random

class FeatureValuesTest {

    private val random = Random(478343412)

    private fun randArray(size: Int): DoubleArray {
        return DoubleArray(size) {
            random.nextDouble()
        }
    }

    @Test
    fun `constructs vector string value`() {
        val tensorBackedString = F.String(
            "my name is",
            TypedRowVector(randArray(101), Dim.VariableF)
        )
        println(tensorBackedString)
    }

    @Test
    fun `combines vector values`() {
        val tensorBackedLong0 = F.Long(
            21L,
            TypedRowVector(randArray(121), Dim.VariableF)
        )
        val tensorBackedLong1 = F.Long(
            25L,
            TypedRowVector(randArray(121), Dim.VariableF)
        )

        val combined = tensorBackedLong0.combineWith(tensorBackedLong1) { (v0, t0), (v1, t1) ->
            (v0.toString() + v1.toString()) to (t0.plus(t1))
        }

        println(combined)
    }

}