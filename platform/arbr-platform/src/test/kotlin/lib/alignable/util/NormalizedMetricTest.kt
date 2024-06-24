package com.arbr.alignable.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

//class NormalizedMetricTest {
//
//    @Test
//    fun `makes values`() {
//
//        val p0 = NormalizedMetric.PosUnbounded(1.4)
//        val u0 = NormalizedMetric.Unit(0.76)
//
//        println(p0.normalizedValue)
//
//        println(p0 + u0)
//        println(u0 + p0)
//
//        assertEquals((p0 + u0).normalizedValue, (u0 + p0).normalizedValue, 1E-8)
//
//        println(u0 sup p0)
//        println(p0 sup u0)
//
//        assertEquals((u0 sup p0).normalizedValue, (p0 sup u0).normalizedValue, 1E-8)
//    }
//
//    @Test
//    fun `supports half life`() {
//        val p0 = NormalizedMetric.PosUnbounded(4.0, 4.0)
//
//        println(p0.normalizedValue)
//
//        assertEquals(0.5, p0.normalizedValue, 1E-8)
//    }
//
//    @Test
//    fun `throws for invalid`() {
//        assertThrows<Exception> {
//            NormalizedMetric.Unit(1.4)
//        }
//        assertThrows<Exception> {
//            NormalizedMetric.Unit(-0.1)
//        }
//        assertThrows<Exception> {
//            NormalizedMetric.PosUnbounded(-0.1)
//        }
//        assertThrows<Exception> {
//            NormalizedMetric.PosUnbounded(0.1, -1.0)
//        }
//    }
//
//    @Test
//    fun `supports limord`() {
//
//        val u0 = NormalizedMetric.Unit(0.76)
//        val limOrd = NormalizedMetric.LimOrd
//
//        assertEquals(limOrd, u0 + limOrd)
//        assertEquals(limOrd, limOrd + u0)
//        assertEquals(limOrd, u0 sup limOrd)
//        assertEquals(limOrd, limOrd sup u0)
//    }
//
//}