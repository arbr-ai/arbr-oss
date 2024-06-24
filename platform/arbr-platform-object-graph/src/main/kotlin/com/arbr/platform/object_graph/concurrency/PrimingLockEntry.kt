package com.arbr.platform.object_graph.concurrency

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

data class LockAcquireFailureInfo(
    val kind: String,
    val resourceUuid: String,
    val resourceTypeDisplayName: String,
    val ownerOperationKey: String,
    val lockLevelRequested: LockLevel,
    val conflictingHolderKey: String,
    val conflictingHolderLockLevel: LockLevel,
    val creationTimestampMs: Long,
)

data class PrimingLockEntry(
    val key: String,
    val ordinal: Long,
    val targetLockLevel: LockLevel,
    val creationTimestampMs: Long,
    val graduateInner: () -> PropertyValueLock,
    val releaseInner: () -> Unit,
    val failInner: (Throwable?) -> Unit,
) {

    /**
     * 0 = new
     * 1 = graduated
     * 2 = released
     * 3 = failed
     */
    val state = AtomicInteger(0)

    private fun setAndCheck(newValue: Int): Boolean {
        val oldValue = state.getAndSet(newValue)
        return oldValue != newValue // didSet
    }

    private var graduatedPropertyValueLock: PropertyValueLock? = null

    @Synchronized
    fun graduate(): PropertyValueLock {
        return if (setAndCheck(1)) {
            val newLock = graduateInner()
            graduatedPropertyValueLock = newLock
            newLock
        } else {
            graduatedPropertyValueLock!!
        }
    }

    fun release() {
        if (setAndCheck(2)) {
            releaseInner()
        }
    }

    fun fail(t: Throwable?) {
        if (setAndCheck(3)) {
            failInner(t)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PrimingLockEntry::class.java)
    }

}

data class PrimingLockAcquireResult(
    val primingLockEntry: PrimingLockEntry?,
    val lockAcquireFailureInfo: LockAcquireFailureInfo?,
    val waitTimeMs: Long?,
)
