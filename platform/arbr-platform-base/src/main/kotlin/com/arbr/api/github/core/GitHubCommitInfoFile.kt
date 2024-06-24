package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCommitInfoFile(
    val sha: String?,
    val filename: String,
    val status: String,
    val additions: Long,
    val deletions: Long,
    val changes: Long,
    val blobUrl: String?,
    val rawUrl: String?,
    val contentsUrl: String,

    /**
     * Actual git patch contents for the change.
     */
    val patch: String?,

    /**
     * For renamed files, the previous file name
     */
    val previousFilename: String?,
)