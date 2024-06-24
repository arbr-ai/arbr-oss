package com.arbr.platform.object_graph.core

enum class ResourceOperationState {

    // Ready
    WAITING_DEPENDENCY_EXPECTED_SATISFIED,
    NEW,
    WAITING_LOCK,
    WAITING_ARBITRATION,
    WAITING_DEPENDENCY,
    WAITING,
    RETRY,

    // In progress
    PROCESSING,
    INFLIGHT,

    // Finished
    SUCCEEDED,
    REDUNDANT,
    FAILED;

    fun isPending(): Boolean {
        return when (this) {
            WAITING_DEPENDENCY_EXPECTED_SATISFIED,
            NEW,
            WAITING_LOCK,
            WAITING_ARBITRATION,
            WAITING_DEPENDENCY,
            WAITING,
            RETRY -> true

            PROCESSING,
            INFLIGHT,
            SUCCEEDED,
            REDUNDANT,
            FAILED -> false
        }
    }

    fun isInProgress(): Boolean {
        return when (this) {
            WAITING_DEPENDENCY_EXPECTED_SATISFIED,
            NEW,
            WAITING_LOCK,
            WAITING_ARBITRATION,
            WAITING_DEPENDENCY,
            WAITING,
            RETRY -> false

            PROCESSING,
            INFLIGHT -> true

            SUCCEEDED,
            REDUNDANT,
            FAILED -> false
        }
    }

    fun isFinished(): Boolean {
        return when (this) {
            WAITING_DEPENDENCY_EXPECTED_SATISFIED,
            NEW,
            WAITING_LOCK,
            WAITING_ARBITRATION,
            WAITING_DEPENDENCY,
            WAITING,
            RETRY,
            PROCESSING,
            INFLIGHT -> false

            SUCCEEDED,
            REDUNDANT,
            FAILED -> true
        }
    }

    companion object {
        val pendingStates = values().filter { it.isPending() }
    }
}