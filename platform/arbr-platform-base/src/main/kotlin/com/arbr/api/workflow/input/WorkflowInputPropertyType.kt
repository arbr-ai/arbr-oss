package com.arbr.api.workflow.input

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowInputPropertyType(
    @JsonValue val kind: String
) {
    TEXT("text"),
    // ... support more ...
}