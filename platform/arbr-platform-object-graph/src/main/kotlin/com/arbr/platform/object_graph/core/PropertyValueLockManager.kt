package com.arbr.platform.object_graph.core

import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.concurrency.LockLevel
import com.arbr.platform.object_graph.concurrency.PropertyValueLock
import com.arbr.platform.object_graph.util.LexIntSequence
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

// Note!
// @Component
class PropertyValueLockManager {

    private val propertyValueLockMap = ConcurrentHashMap<ProposedValueStreamIdentifierKey, PropertyValueLock>()
    private val ordinalCounterMap = ConcurrentHashMap<String, Int>()

    private fun hashOrdinalComponent(
        operationKey: String
    ): Int {
        return "some_salt:$operationKey".hashCode().absoluteValue
    }

    private fun incrementAndGetOperationOrdinal(
        operationKey: String
    ): OperationLockAttemptKey {
        val ordinal = ordinalCounterMap.compute(operationKey) { _, v ->
            if (v == null) {
                0
            } else {
                v + 1
            }
        }!!

        val ordinalSeq = LexIntSequence(listOf(ordinal, hashOrdinalComponent(operationKey)))
        return OperationLockAttemptKey(ordinalSeq)
    }

    private fun newLock(
        targetKey: ProposedValueStreamIdentifierKey,
    ): PropertyValueLock {
        return PropertyValueLock(targetKey)
    }

    private fun acquireSingle(
        operationLockAttemptKey: OperationLockAttemptKey,
        target: PropertyIdentifier,
        lockLevel: LockLevel,
    ): PropertyValueLock {
        val targetKey = ProposedValueStreamIdentifierKey(
            target.resourceUuid,
            target.propertyKey.name,
            target.relationship.name,
            target.resourceKey.name,
        )

        val lock = propertyValueLockMap.computeIfAbsent(targetKey) {
            newLock(it)
        }

        lock.tryAcquire(operationLockAttemptKey, lockLevel)

        return lock
    }

    fun withAttempt(
        operationKey: String,
        operation: (PropertyValueLocker) -> Mono<Void>
    ): Mono<Void> {
        val operationLockAttemptKey = incrementAndGetOperationOrdinal(operationKey)

        val locker = object : PropertyValueLocker {
            private val locks = mutableListOf<PropertyValueLock>()

            override fun acquireRead(proposedValueStreamIdentifier: PropertyIdentifier) {
                synchronized(locks) {
                    val lock = acquireSingle(operationLockAttemptKey, proposedValueStreamIdentifier, LockLevel.READ)
                    locks.add(lock)
                }
            }

            override fun acquireWrite(proposedValueStreamIdentifier: PropertyIdentifier) {
                synchronized(locks) {
                    val lock = acquireSingle(operationLockAttemptKey, proposedValueStreamIdentifier, LockLevel.WRITE)
                    locks.add(lock)
                }
            }

            override fun releaseAll() {
                synchronized(locks) {
                    locks.forEach {
                        it.release(operationLockAttemptKey)
                    }

                    locks.clear()
                }
            }
        }

        return Mono.using(
            { locker },
            { operation(it) },
            { it.releaseAll() }
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PropertyValueLockManager::class.java)
    }
}