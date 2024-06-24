package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowViewModelStatusKind(@JsonValue val serializedName: String) {
    BOOTING("booting"),
    PLANNING("planning"),
    DEVELOPING("developing"),
    TESTING("testing"),
    DEPLOYING("deploying"),
    FINISHING("finishing"),
    NOT_STARTED("not_started"),
    SUCCEEDED("succeeded"),
    CANCELLED("cancelled"),
    FAILED("failed"),
    UNKNOWN("unknown");
}
