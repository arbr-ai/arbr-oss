package com.arbr.og_engine.artifact

import com.arbr.api.workflow.core.WorkflowStatus

/**
 * Artifact signaling a status change.
 */
data class StatusArtifact(
    val status: WorkflowStatus,
    val value: String?,
    val throwable: Throwable?,
): Artifact
