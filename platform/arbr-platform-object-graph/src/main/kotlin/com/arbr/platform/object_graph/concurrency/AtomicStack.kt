package com.arbr.platform.object_graph.concurrency

import org.slf4j.LoggerFactory
import java.util.*

class AtomicStack<T>(
    private val initialValue: T,
    private val logId: String? = null,
) {

    private val stack = Stack<T>()

    @Synchronized
    fun push(item: T): T {
        return stack.push(item)
    }

    @Synchronized
    fun pop(): T {
        return stack.pop()
    }

    @Synchronized
    fun peek(): T? {
        return try {
            stack.peek()
        } catch (e: EmptyStackException) {
            null
        }
    }

    @Synchronized
    private fun <U> use(f: (Stack<T>) -> U): U {
        return f(stack)
    }

    /**
     * Expose an interface similar to AtomicEnum
     */

    fun get(): T {
        return peek() ?: initialValue
    }

    fun compareAndExchange(expectedValue: T, newValue: T): T {
        return use { stack ->
            val n = stack.size
            val lastElement = if (n > 0) {
                stack[n - 1]
            } else {
                initialValue
            }

            if (lastElement == expectedValue) {
                stack.push(newValue)
                com.arbr.platform.object_graph.concurrency.AtomicStack.Companion.logger.debug("CAS {} = {}", logId, newValue)
            } else {
                com.arbr.platform.object_graph.concurrency.AtomicStack.Companion.logger.debug("CAS_FAIL {} = {}; EXPECTED {}; GOT {}", logId, newValue, expectedValue, lastElement)
            }

            lastElement
        }
    }

    fun compareAndExchangePush(expectedValue: T, newValue: T): Boolean {
        val witness = compareAndExchange(expectedValue, newValue)
        return witness == expectedValue
    }

    /**
     * Pop the latest value and, if it matches the expected value, push the new value.
     *
     * Returns whether the latest value matched the expected value.
     */
    @Synchronized
    fun popAndPushConditionally(expectedValue: T, newValue: T): Boolean {
        return if (popConditionally(expectedValue) == null) {
            false
        } else {
            push(newValue)
            true
        }
    }

    /**
     * Get a value at `negativeIndex` - i.e. -1 corresponds to the current value, -2 to the value before it, etc.
     * throws IndexOutOfBoundsException
     */
    fun getPriorValue(negativeIndex: Int = -2): T {
        if (negativeIndex >= 0) {
            throw IndexOutOfBoundsException("Reverse index into stack should be negative, got $negativeIndex")
        }

        return use {
            val n = stack.size
            val i = n + negativeIndex
            if (i < 0) {
                initialValue
            } else {
                stack[i]
            }
        }
    }

    /**
     * Revert to the previous value by popping off the stack.
     *
     * Returns the (now current) prior value if successful, or else null if the current value does not match the
     * expected value.
     */
    fun popConditionally(expectedState: T): T? {
        return use {
            val currentValue = get()
            val priorValue = getPriorValue()
            if (expectedState == currentValue) {
                pop()
                priorValue
            } else {
                null
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(com.arbr.platform.object_graph.concurrency.AtomicStack::class.java)
    }

}