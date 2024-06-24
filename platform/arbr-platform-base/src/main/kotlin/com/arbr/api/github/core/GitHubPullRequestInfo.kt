package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Detailed info on a merged pull request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubPullRequestInfo(
    val id: Long,
    val title: String,
    val body: String?,
    val diffUrl: String,
    val htmlUrl: String,
    val patchUrl: String,
    val mergeCommitSha: String?,
    val commitsUrl: String,
)
