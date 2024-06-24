package com.arbr.api.workflow.resource

import com.fasterxml.jackson.annotation.JsonValue

data class WorkflowResourceType(
    @JsonValue val serializedName: String
)
