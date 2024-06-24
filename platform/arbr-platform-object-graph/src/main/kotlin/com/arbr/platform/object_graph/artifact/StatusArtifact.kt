package com.arbr.og_engine.artifact

import com.arbr.platform.object_graph.artifact.WorkflowStatus

/**
 * Artifact signaling a status change.
 */
data class StatusArtifact(
    val status: WorkflowStatus,
    val value: String?,
    val throwable: Throwable?,
): Artifact
