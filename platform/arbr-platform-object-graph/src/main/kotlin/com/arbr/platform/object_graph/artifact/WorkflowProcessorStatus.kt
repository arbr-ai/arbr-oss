package com.arbr.platform.object_graph.artifact

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowProcessorStatus(@JsonValue val serializedName: String) {
    READY("not_started"),
    STARTED("started"),
    SUCCEEDED("succeeded"),
    FAILED("failed"),
    CANCELLED("cancelled");
}
