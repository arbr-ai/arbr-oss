package com.arbr.platform.object_graph.artifact

/**
 * An update on the properties an individual resource
 */
data class WorkflowResourceUpdateArtifact(
    val objectModelUuid: String,
    val resourceType: WorkflowResourceType,
    val resourceData: Map<String, Any?>,
): Artifact
