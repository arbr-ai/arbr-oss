package com.arbr.alignable.alignable

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.arbr.platform.alignable.alignable.collections.AlignableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequenceAlignmentUtilsTest {

    private fun stringAList(str: String) = AlignableList(str.toList().map { AtomicAlignable(it) })

    @Test
    fun `aligns sequences`() {
        val source = stringAList("apple")
        val target = stringAList("apoplexy")

        val alignment = source.align(target)
        println(jacksonObjectMapper().writeValueAsString(alignment))

        assertEquals(6, alignment.operations.size)
        alignment.operations[1].run {
            assertEquals("Edit", this::class.java.simpleName)
            assertEquals(2, atIndex)
            assertEquals('o', element.element)
        }
        alignment.operations[3].run {
            assertEquals("Edit", this::class.java.simpleName)
            assertEquals(6, atIndex)
            assertEquals('x', element.element)
        }
        alignment.operations[5].run {
            assertEquals("Edit", this::class.java.simpleName)
            assertEquals(7, atIndex)
            assertEquals('y', element.element)
        }
    }

    @Test
    fun `aligns sequences 2`() {
        val source = stringAList("dfgdfgdfvdsav")
        val target = stringAList("gdaaaaaafgsav")

        val alignment = source.align(target)
        println(alignment)

        val reapplied = source.applyAlignment(alignment.operations)
        assertEquals(target.map { it.element }, reapplied.map { it.element })
    }

    @Test
    fun `aligns sequences 3`() {
        val source = stringAList("dfgdsdqadwwqdwqdqwdedfvxzcpxcxcxzczxwqoqweiqwqwqqqwwqeagdfvdsav")
        val target = stringAList("gdaaaaadwwqdwqdqwdedfvaafasascvvvnmanaaasxcgsuiuiuykyukav")

        val alignment = source.align(target)
        println(alignment)

        val reapplied = source.applyAlignment(alignment.operations)
        assertEquals(target.map { it.element }, reapplied.map { it.element })
    }
}
