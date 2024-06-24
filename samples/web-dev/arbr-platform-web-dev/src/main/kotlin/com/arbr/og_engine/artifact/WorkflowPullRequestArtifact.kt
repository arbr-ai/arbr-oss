package com.arbr.og_engine.artifact

/**
 * Representation of an output of the build process. For example a Pull Request.
 */
data class WorkflowPullRequestArtifact(
    val pullRequestId: Long,
    val title: String,
    val link: String,
    val body: String?,
    val mergeCommitSha: String?,
) : WorkflowArtifact()
