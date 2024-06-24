package com.arbr.og_engine.artifact

import com.arbr.api.workflow.event.event_data.common.DiffStat

data class WorkflowCommitArtifact(
    val commitHash: String,
    val commitMessage: String,
    val commitUrl: String,
    val diffSummary: String?,
    val diffStat: DiffStat?,
) : WorkflowArtifact()
