package com.arbr.ml.linear.typed.tensor.base

import com.arbr.platform.ml.linear.typed.tensor.base.TensorTypeTree
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TensorTypeTreeTest {

    @Test
    fun `constructs type tree`() {
        val nodes = (0..<10).map { i ->
            TensorTypeTree.Leaf("t$i")
        }

        val p0 = TensorTypeTree.ProductOf(nodes[0], nodes[2])
        val p1 = TensorTypeTree.ProductOf(nodes[1], nodes[3])
        val p2 = TensorTypeTree.ProductOf(nodes[4], nodes[7])

        val s0 = TensorTypeTree.SumOf(nodes[5], p0)
        val s1 = TensorTypeTree.ProductOf(p1, s0)
        val s2 = TensorTypeTree.SumOf(p1, p2)

        val s3 = TensorTypeTree.SumOf(s1, s2)
        val root = TensorTypeTree.SumOf(s0, s3)

        val descriptor = root.getDescriptor()
        val expectedResult = "{t5, t0 x t2, t1 x t3 x {t5, t0 x t2}, t1 x t3, t4 x t7}"
        println(descriptor)
        Assertions.assertEquals(expectedResult, descriptor)
    }

}