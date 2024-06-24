package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCommitSummary(
    val sha: String,
    val commit: GitHubCommitInfoInnerCommit,
    val url: String,
)
