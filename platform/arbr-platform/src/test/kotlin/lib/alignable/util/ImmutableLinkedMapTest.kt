package com.arbr.alignable.util

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ImmutableLinkedMapTest {
    @Test
    fun `initializes from list`() {
        val pairList = listOf(
            "a" to 0,
            "c" to 1,
            "b" to 2,
            "e" to 3,
        )
        val ilm = ImmutableLinkedMap(
            pairList
        )

        assertEquals(4, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = pairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `initializes from vararg`() {
        val pairList = listOf(
            "a" to 0,
            "c" to 1,
            "b" to 2,
            "e" to 3,
        )

        val ilm = ImmutableLinkedMap(
            "a" to 0,
            "c" to 1,
            "b" to 2,
            "e" to 3,
        )

        assertEquals(4, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = pairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `deduplicates from list`() {
        val ilm = ImmutableLinkedMap(
            listOf(
                "a" to 0,
                "b" to 1,
                "b" to 2,
                "e" to 3,
            )
        )

        val expectedPairList = listOf(
            "a" to 0,
            "b" to 1,
            "e" to 3,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `deduplicates from vararg`() {
        val ilm = ImmutableLinkedMap(
            "a" to 0,
            "b" to 1,
            "b" to 2,
            "e" to 3,
        )

        val expectedPairList = listOf(
            "a" to 0,
            "b" to 1,
            "e" to 3,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }


    @Test
    fun `adds single`() {
        val ilm0 = ImmutableLinkedMap(
            listOf(
                "a" to 0,
                "b" to 1,
                "b" to 2,
                "e" to 3,
            )
        )

        val ilm = ilm0.adding("f", -1)

        val expectedPairList = listOf(
            "a" to 0,
            "b" to 1,
            "e" to 3,
            "f" to -1,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `adds single duplicate updates`() {
        val ilm0 = ImmutableLinkedMap(
            listOf(
                "a" to 0,
                "b" to 1,
                "b" to 2,
                "e" to 3,
            )
        )

        val ilm = ilm0.adding("a", -1)

        val expectedPairList = listOf(
            "a" to -1,
            "b" to 1,
            "e" to 3,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `adds many`() {
        val ilm0 = ImmutableLinkedMap(
            listOf(
                "a" to 0,
                "b" to 1,
                "b" to 2,
                "e" to 3,
            )
        )

        val ilm = ilm0.updatingFromPairs(
            listOf(
                "f" to -1,
                "0" to 11,
            )
        )

        val expectedPairList = listOf(
            "a" to 0,
            "b" to 1,
            "e" to 3,
            "f" to -1,
            "0" to 11,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }

    @Test
    fun `adds many duplicate updates`() {
        val ilm0 = ImmutableLinkedMap(
            listOf(
                "a" to 0,
                "b" to 1,
                "b" to 2,
                "e" to 3,
            )
        )

        val ilm = ilm0.updatingFromPairs(
            listOf(
                "f" to -1,
                "b" to 2,
                "a" to -1,
            )
        )

        val expectedPairList = listOf(
            "a" to -1,
            "b" to 2,
            "e" to 3,
            "f" to -1,
        )
        assertEquals(expectedPairList.size, ilm.size)

        for ((i, p) in ilm.entries.withIndex()) {
            val (k, v) = expectedPairList[i]
            assertEquals(k, p.key)
            assertEquals(v, p.value)
        }
    }
}