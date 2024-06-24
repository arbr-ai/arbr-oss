package com.arbr.platform.object_graph.core

data class WorkflowOperationStatusInfo(
    val resourceOperations: List<ResourceOperationStatus<*, *, *>>,
    // Function name to dependency display names
    val pendingOperations: Map<String, List<String>>,
    val inFlightOperationCount: Int,
    val inFlightOperations: Map<String, List<String>>,
    val completedOperationCount: Int,
    val completedOperations: Map<String, List<String>>,
)
