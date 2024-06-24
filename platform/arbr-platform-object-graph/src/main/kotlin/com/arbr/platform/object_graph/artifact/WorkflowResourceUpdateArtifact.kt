package com.arbr.og_engine.artifact

import com.arbr.platform.object_graph.artifact.WorkflowResourceType

/**
 * An update on the properties an individual resource
 */
data class WorkflowResourceUpdateArtifact(
    val objectModelUuid: String,
    val resourceType: WorkflowResourceType,
    val resourceData: Map<String, Any?>,
): Artifact
