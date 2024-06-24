package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCommitInfoInnerCommit(
    val message: String,
)