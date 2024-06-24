package com.arbr.alignable.alignable

import com.arbr.platform.alignable.alignable.collections.AlignableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue
import kotlin.random.Random

class AlignableListTest {

    @Test
    fun `aligns lists`() {
        val n = 1551
        val rand = Random(1239876124L)
        val list0 = AlignableList<AtomicAlignable<Char>, Char>(
            (0 until n - 25).map { AtomicAlignable(Char(rand.nextInt().absoluteValue % 256)) }
        )
        val list1 = AlignableList(
            (0 until n).map { AtomicAlignable(Char(rand.nextInt().absoluteValue % 256)) }
        )

        val alignment = list0.align(list1)
        assertEquals(5486, alignment.operations.size)
        assertEquals(5486, alignment.cost.toInt())
        println(alignment.operations.size)
        val reapplied = list0.applyAlignment(alignment.operations)
        assertEquals(list1.map { it.element }, reapplied.map { it.element })
    }

    @Test
    fun `aligns lists from empty`() {
        val n = 3131
        val rand = Random(1239876124L)
        val list0 = AlignableList<AtomicAlignable<Char>, Char>(
            emptyList()
        )
        val list1 = AlignableList(
            (0 until n).map { AtomicAlignable(Char(rand.nextInt().absoluteValue % 256)) }
        )

        val alignment = list0.align(list1)
        println(alignment.operations.size)
        val reapplied = list0.applyAlignment(alignment.operations)
        assertEquals(list1.map { it.element }, reapplied.map { it.element })
    }

    @Test
    fun `aligns equal lists`() {
        val n = 100
        val rand = Random(812319283L)
        val list0 = AlignableList<AtomicAlignable<Char>, Char>(
            (0 until n).map { AtomicAlignable(Char(rand.nextInt().absoluteValue % 256)) }
        )
        val list1 = AlignableList(
            (0 until n).map { list0[it] }
        )

        val alignment = list0.align(list1)
        println(alignment.operations.size)
        val reapplied = list0.applyAlignment(alignment.operations)
        assertEquals(list1.map { it.element }, reapplied.map { it.element })
    }
}
