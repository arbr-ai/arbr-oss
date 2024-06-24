package com.arbr.platform.object_graph.artifact

import com.fasterxml.jackson.annotation.JsonValue

data class WorkflowResourceType(
    @JsonValue val serializedName: String
)
