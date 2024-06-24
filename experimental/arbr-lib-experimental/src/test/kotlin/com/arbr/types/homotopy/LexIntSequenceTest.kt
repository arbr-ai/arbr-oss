package com.arbr.types.homotopy

import com.arbr.util_common.LexIntSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LexIntSequenceTest {

    @Test
    fun `orders sequences`() {
        val seq0 = LexIntSequence(listOf(1, 2))
        val seq1 = LexIntSequence(listOf(1, 2, 3))
        Assertions.assertTrue(seq0 < seq1)
    }

    @Test
    fun `orders sequences 2`() {
        val seq0 = LexIntSequence(listOf(0, 0))
        val seq1 = LexIntSequence(listOf(0))
        Assertions.assertTrue(seq0 > seq1)
    }

    @Test
    fun `orders sequences 3`() {
        val seq0 = LexIntSequence(listOf(0, 0))
        val seq1 = LexIntSequence(listOf(0, 1))
        Assertions.assertTrue(seq0 < seq1)
    }

}