package com.arbr.platform.object_graph.artifact

/**
 * Artifact signaling a status change.
 */
data class StatusArtifact(
    val status: WorkflowStatus,
    val value: String?,
    val throwable: Throwable?,
): Artifact
