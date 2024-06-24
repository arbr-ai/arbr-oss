package com.arbr.platform.object_graph.concurrency

import com.arbr.platform.object_graph.core.OperationLockAttemptKey

class LockAcquireException(
    val kind: String,
    val resourceUuid: String,
    val resourceTypeDisplayName: String,
    val ownerOperationKey: OperationLockAttemptKey,
    val lockLevelRequested: LockLevel,
    val conflictingHolderKey: OperationLockAttemptKey,
    val conflictingHolderLockLevel: LockLevel,
    val conflictingHolderCreationTimestampMs: Long,
) : Exception("Lock unavailable on ${resourceTypeDisplayName}:$resourceUuid for $ownerOperationKey - held by $conflictingHolderKey")