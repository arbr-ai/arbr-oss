package com.arbr.platform.object_graph.artifact

data class WorkflowResourceCreationArtifact(
    val objectModelUuid: String,
    val parentObjectModelUuid: String?,
    val resourceType: WorkflowResourceType,
    val resourceData: Map<String, Any?>,
): Artifact
