package com.arbr.og.object_model.common.model

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ProposalImpl<S: Any>(
    override val acceptedValue: S?,
    override val proposedValue: S?,
    val acceptFun: () -> Unit,
    val rejectFun: () -> Unit,
): Proposal<S> {
    // 0 = unresolved, 1 = accepted, 2 = rejected
    private val isResolved = AtomicInteger(0)

    override fun accept() {
        // Check-and-set for idempotency
        val didSet = isResolved.compareAndSet(0, 1)
        if (didSet) {
            acceptFun()
        }
    }

    override fun reject() {
        // Check-and-set for idempotency
        val didSet = isResolved.compareAndSet(0, 2)
        if (didSet) {
            rejectFun()
        }
    }
}