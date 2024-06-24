package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCreateRepoRequest(
    val name: String,
    val description: String? = null,
    val homepage: String? = null,
    val private: Boolean? = null,
    val visibility: String? = null,
    val hasIssues: Boolean? = null,
    val hasProjects: Boolean? = null,
    val hasWiki: Boolean? = null,
    val hasDownloads: Boolean? = null,
    val isTemplate: Boolean? = null,
    val teamId: Int? = null,
    val autoInit: Boolean? = null,
    val gitIgnoreTemplate: String? = null,
    val licenseTemplate: String? = null,
    val allowSquashMerge: Boolean? = null,
    val allowMergeCommit: Boolean? = null,
    val allowRebaseMerge: Boolean? = null,
    val allowAutoMerge: Boolean? = null,
    val deleteBranchOnMerge: Boolean? = null,
    val useSquashPrTitleAsDefault: Boolean? = null,
    val squashMergeCommitTitle: String? = null,
    val squashMergeCommitMessage: String? = null,
    val mergeCommitTitle: String? = null,
    val mergeCommitMessage: String? = null,
)

sealed class GitHubCreateRepoResult {
    class Created(val repoSummary: GitHubRepoSummary): GitHubCreateRepoResult()
    object Exists: GitHubCreateRepoResult()
    object Forbidden: GitHubCreateRepoResult()
}
