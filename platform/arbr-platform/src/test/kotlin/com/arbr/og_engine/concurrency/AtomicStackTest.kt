package com.arbr.og_engine.concurrency

import com.arbr.og_engine.core.ResourceOperationState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AtomicStackTest {

    @Test
    fun `sets and gets`() {
        val av = AtomicStack<ResourceOperationState>(ResourceOperationState.NEW)

        av.push(ResourceOperationState.PROCESSING)
        val state = av.get()
        Assertions.assertEquals(ResourceOperationState.PROCESSING, state)
    }

    @Test
    fun `compares and exchanges`() {
        val av = AtomicStack<ResourceOperationState>(ResourceOperationState.NEW)

        val witness = av.compareAndExchange(ResourceOperationState.NEW, ResourceOperationState.PROCESSING)
        Assertions.assertEquals(ResourceOperationState.NEW, witness)

        val state = av.get()
        Assertions.assertEquals(ResourceOperationState.PROCESSING, state)
    }

    @Test
    fun `compares and exchanges not equal`() {
        val av = AtomicStack<ResourceOperationState>(ResourceOperationState.NEW)

        val witness = av.compareAndExchange(ResourceOperationState.WAITING, ResourceOperationState.PROCESSING)
        // was new
        Assertions.assertEquals(ResourceOperationState.NEW, witness)

        val state = av.get()
        // stays new
        Assertions.assertEquals(ResourceOperationState.NEW, state)
    }

    @Test
    fun `reverts value`() {
        val av = AtomicStack<ResourceOperationState>(ResourceOperationState.NEW)

        av.push(ResourceOperationState.PROCESSING)
        val state = av.get()
        Assertions.assertEquals(ResourceOperationState.PROCESSING, state)

        val reverted = av.popConditionally(ResourceOperationState.PROCESSING)
        Assertions.assertEquals(ResourceOperationState.NEW, reverted)

        val state2 = av.get()
        // stays new
        Assertions.assertEquals(ResourceOperationState.NEW, state2)
    }

}
