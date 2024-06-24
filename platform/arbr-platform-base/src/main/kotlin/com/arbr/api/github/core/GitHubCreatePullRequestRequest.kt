package com.arbr.api.github.core

data class GitHubCreatePullRequestRequest(
    val title: String,
    val head: String,
    val base: String,
    val body: String,
)