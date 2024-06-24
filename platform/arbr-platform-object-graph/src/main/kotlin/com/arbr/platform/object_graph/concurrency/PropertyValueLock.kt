package com.arbr.platform.object_graph.concurrency

import com.arbr.platform.object_graph.core.OperationLockAttemptKey
import com.arbr.platform.object_graph.core.ProposedValueStreamIdentifierKey
import com.arbr.util.collections.splitOn
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

data class PropertyValueLock(
    val key: ProposedValueStreamIdentifierKey,
) {
    private val resourceUuid: String = key.parentUuid
    private val resourceTypeDisplayName: String = key.resourceType

    /**
     * Map of owner key to actual lock level held and creation timestamp.
     */
    private val holders = ConcurrentHashMap<OperationLockAttemptKey, Pair<LockLevel, Long>>()

    private fun conflicts(
        operationKey: OperationLockAttemptKey,
        lockLevel: LockLevel,
    ): List<Map.Entry<OperationLockAttemptKey, Pair<LockLevel, Long>>> {
        return when (lockLevel) {
            LockLevel.READ -> holders.entries.filter { (key, otherLevel) -> key != operationKey && otherLevel.first == LockLevel.WRITE }
            LockLevel.WRITE -> holders.entries.filter { (key, _) -> key != operationKey }
        }
    }

    @Synchronized
    private fun pruneHolders(
        holdersToPrune: List<OperationLockAttemptKey>,
    ) {
        holdersToPrune.forEach { holders.remove(it) }
    }

    @Synchronized
    fun tryAcquire(
        operationKey: OperationLockAttemptKey,
        lockLevel: LockLevel,
    ) {
        val ourExistingHolder = holders[operationKey]
        if (ourExistingHolder != null && ourExistingHolder.first >= lockLevel) {
            // No new lock needed
            return
        }

        val conflicts = conflicts(operationKey, lockLevel)

        val (validConflicts, oldConflicts) = conflicts.splitOn { (key, lockPair) ->
            // Hack: Ignore conflicts and prune very stale (10s) locks
            // These shouldn't exist beyond the lifecycle of a processor if we are diligent with releasing locks
            // TODO: Diagnose why these exist and fix root cause
            val ageMs = Instant.now().toEpochMilli() - lockPair.second
            if (ageMs >= AGE_LIMIT_MS) {
                logger.warn("Conflict against old lock ${key.ordinalSeq.ints} age ${ageMs}ms on ${resourceTypeDisplayName}[${resourceUuid}]; pruning")
            }

            ageMs < AGE_LIMIT_MS
        }

        if (oldConflicts.isNotEmpty()) {
            pruneHolders(oldConflicts.map { it.key })
        }

        val conflict = validConflicts.firstOrNull()
        if (conflict != null) {
            val (conflictLockLevel, conflictCreationTimestamp) = conflict.value

            throw LockAcquireException(
                "acquire_conflict",
                resourceUuid,
                resourceTypeDisplayName,
                operationKey,
                lockLevel,
                conflict.key,
                conflictLockLevel,
                conflictCreationTimestamp,
            )
        } else {
            // No other obstructing locks exist
            holders[operationKey] = lockLevel to Instant.now().toEpochMilli()
        }
    }

    @Synchronized
    fun level(
        operationKey: OperationLockAttemptKey,
    ): LockLevel? {
        return holders[operationKey]?.first
    }

    @Synchronized
    fun release(
        operationKey: OperationLockAttemptKey,
    ): Int {
        holders.remove(operationKey)
        return holders.size
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PropertyValueLock::class.java)

        private const val AGE_LIMIT_MS = 10000L
    }
}