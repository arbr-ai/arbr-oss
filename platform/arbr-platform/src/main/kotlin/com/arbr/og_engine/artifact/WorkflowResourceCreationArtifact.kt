package com.arbr.og_engine.artifact

import com.arbr.api.workflow.resource.WorkflowResourceType

data class WorkflowResourceCreationArtifact(
    val objectModelUuid: String,
    val parentObjectModelUuid: String?,
    val resourceType: WorkflowResourceType,
    val resourceData: Map<String, Any?>,
): Artifact
