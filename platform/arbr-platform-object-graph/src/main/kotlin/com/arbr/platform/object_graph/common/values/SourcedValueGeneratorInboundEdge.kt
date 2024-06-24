package com.arbr.og.object_model.common.values

data class SourcedValueGeneratorInboundEdge(
    val applicationId: String?,
    val completionCacheKey: String?,
    val operationId: String?,
    val parentValueIds: List<String>,
)