package com.arbr.platform.object_graph.util

import org.slf4j.LoggerFactory

internal class AtomicValue<T: Any> {

    class InvalidLockException: Exception("Attempted release with invalid lock; programming error or race condition")

    data class Lock(
        private val innerRelease: (Lock) -> Unit
    ) {
        fun release() = innerRelease(this)
    }

    private var lock: Lock? = null
    private var value: T? = null

    @Synchronized
    fun getOrSet(f: () -> T): Pair<Lock?, T> {
        val existingValue = value
        return if (lock == null || existingValue == null) {
            val newLock = Lock { this.release(it) }
            val newValue = f()

            lock = newLock
            value = newValue

            newLock to newValue
        } else {
            null to existingValue
        }
    }

    @Synchronized
    fun release(lock: Lock) {
        if (this.lock == null) {
            logger.warn("Attempted no-op AtomicValue lock release")
        } else if (lock == this.lock) {
            this.lock = null
        } else {
            throw InvalidLockException()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AtomicValue::class.java)
    }

}