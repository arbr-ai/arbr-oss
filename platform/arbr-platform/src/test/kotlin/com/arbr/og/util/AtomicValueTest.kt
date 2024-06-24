package com.arbr.og.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AtomicValueTest {

    @Test
    fun sets() {
        val av = AtomicValue<Int>()
        val (lock, result) = av.getOrSet { 0 }

        Assertions.assertNotNull(lock)
        Assertions.assertEquals(0, result)
    }

    @Test
    fun `respects locked value`() {
        val av = AtomicValue<Int>()
        val (lock, result) = av.getOrSet { 0 }
        val (lock2, result2) = av.getOrSet { 1 }

        Assertions.assertNotNull(lock)
        Assertions.assertEquals(0, result)
        Assertions.assertNull(lock2)
        Assertions.assertEquals(0, result2) // Uses locked value
    }

    @Test
    fun `releases lock value`() {
        val av = AtomicValue<Int>()
        val (lock, result) = av.getOrSet { 0 }
        Assertions.assertNotNull(lock)
        Assertions.assertEquals(0, result)

        val (lock2, result2) = av.getOrSet { 1 }
        Assertions.assertNull(lock2)
        Assertions.assertEquals(0, result2) // Uses locked value

        lock!!.release()
        val (lock3, result3) = av.getOrSet { 2 }
        Assertions.assertNotNull(lock3)
        Assertions.assertEquals(2, result3)
    }

}
