package com.arbr.api.github.core

data class GitHubCommitInfoStats(
    val total: Long,
    val additions: Long,
    val deletions: Long,
)