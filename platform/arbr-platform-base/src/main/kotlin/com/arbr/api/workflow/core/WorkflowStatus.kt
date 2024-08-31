package com.arbr.api.workflow.core

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowStatus(@JsonValue val serializedName: String) {
    NOT_STARTED("not_started"),
    STARTED("started"),
    SUCCEEDED("succeeded"),
    FAILED("failed"),
    CANCELLED("cancelled");

    /**
     * Whether the state is terminal. i.e. we don't expect it to ever change again.
     */
    @JsonIgnore
    fun isTerminal(): Boolean {
        return when (this) {
            NOT_STARTED -> false
            STARTED -> false
            SUCCEEDED -> true
            FAILED -> true
            CANCELLED -> true
        }
    }
}
