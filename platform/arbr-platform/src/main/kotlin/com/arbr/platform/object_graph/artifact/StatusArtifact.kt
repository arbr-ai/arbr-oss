package com.arbr.platform.object_graph.artifact

import com.arbr.api.workflow.core.WorkflowStatus

/**
 * Artifact signaling a status change.
 */
data class StatusArtifact(
    val status: WorkflowStatus,
    val value: String?,
    val throwable: Throwable?,
): Artifact
