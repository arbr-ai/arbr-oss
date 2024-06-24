package com.arbr.og.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class AtomicMonoValueTest {

    @Test
    fun `creates new mono`() {
        val amv = AtomicMonoValue<Int>()
        val newMono = amv.getOrCreate { lock ->
            Mono.fromCallable {
                lock.release()
                0
            }
        }

        assertEquals(0, newMono.block()!!)
    }

    @Test
    fun `respects existing mono`() {
        val amv = AtomicMonoValue<Int>()
        val newMono = amv.getOrCreate { lock ->
            Mono.fromCallable {
                lock.release()
                0
            }
        }

        val secondMono = amv.getOrCreate {
            Mono.fromCallable {
                fail()
            }
        }

        assertEquals(0, newMono.block()!!)
        assertEquals(0, secondMono.block()!!)
    }

    @Test
    fun `releases locked mono`() {
        val amv = AtomicMonoValue<Int>()
        val newMono = amv.getOrCreate { lock ->
            Mono.fromCallable {
                lock.release()
                0
            }
        }

        val secondMono = amv.getOrCreate {
            Mono.fromCallable {
                fail()
            }
        }

        assertEquals(0, newMono.block()!!)
        assertEquals(0, secondMono.block()!!)

        val thirdMono = amv.getOrCreate { lock ->
            Mono.fromCallable {
                lock.release()
                1
            }
        }

        assertEquals(1, thirdMono.block()!!)
    }

}
