package com.arbr.model_loader.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * GitHub data models, copied from the topdown repo
 */
internal object GitHub {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GitHubCommitInfoInnerCommit(
        val message: String,
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class GitHubCommitInfoStats(
        val total: Long,
        val additions: Long,
        val deletions: Long,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class GitHubCommitInfo(
        val sha: String,
        val commit: GitHubCommitInfoInnerCommit,
        val stats: GitHubCommitInfoStats,
        val files: List<GitHubCommitInfoFile>,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
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
    )

    /**
     * Detailed info on a merged pull request.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
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

    data class GitHubCommitRecord(
        val commit: GitHubCommitInfo
    )

    data class GitHubPullRequestRecord(
        val pullRequest: GitHubPullRequestInfo,
        val commits: List<GitHubCommitRecord>,
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GitHubRepoLicense(
        val key: String,
        val name: String,
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GitHubRepoOwner(
        val login: String,
        val id: Int,
        val nodeId: String,
        val avatarUrl: String,
        val gravatarId: String,
        val url: String,
        val htmlUrl: String,
        val followersUrl: String,
        val followingUrl: String,
        val gistsUrl: String,
        val starredUrl: String,
        val subscriptionsUrl: String,
        val organizationsUrl: String,
        val reposUrl: String,
        val eventsUrl: String,
        val receivedEventsUrl: String,
        val type: String,
        val siteAdmin: Boolean
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class GitHubRepoDetail(
        val id: Int,
        val nodeId: String,
        val name: String,
        val fullName: String,
        val private: Boolean,
        val owner: GitHubRepoOwner,
        val htmlUrl: String,
        val description: String?,
        val fork: Boolean,
        val url: String,
        val forksUrl: String,
        val keysUrl: String,
        val collaboratorsUrl: String,
        val teamsUrl: String,
        val hooksUrl: String,
        val issueEventsUrl: String,
        val eventsUrl: String,
        val assigneesUrl: String,
        val branchesUrl: String,
        val tagsUrl: String,
        val blobsUrl: String,
        val gitTagsUrl: String,
        val gitRefsUrl: String,
        val treesUrl: String,
        val statusesUrl: String,
        val languagesUrl: String,
        val stargazersUrl: String,
        val contributorsUrl: String,
        val subscribersUrl: String,
        val subscriptionUrl: String,
        val commitsUrl: String,
        val gitCommitsUrl: String,
        val commentsUrl: String,
        val issueCommentUrl: String,
        val contentsUrl: String,
        val compareUrl: String,
        val mergesUrl: String,
        val archiveUrl: String,
        val downloadsUrl: String,
        val issuesUrl: String,
        val pullsUrl: String,
        val milestonesUrl: String,
        val notificationsUrl: String,
        val labelsUrl: String,
        val releasesUrl: String,
        val deploymentsUrl: String,
        val createdAt: String,
        val updatedAt: String,
        val pushedAt: String,
        val gitUrl: String,
        val sshUrl: String,
        val cloneUrl: String,
        val svnUrl: String,
        val homepage: String?,
        val size: Int,
        val stargazersCount: Int,
        val watchersCount: Int,
        val language: String?,
        val hasIssues: Boolean,
        val hasProjects: Boolean,
        val hasDownloads: Boolean,
        val hasWiki: Boolean,
        val hasPages: Boolean,
        val hasDiscussions: Boolean,
        val forksCount: Int,
        val mirrorUrl: String?,
        val archived: Boolean,
        val disabled: Boolean,
        val openIssuesCount: Int,
        val license: GitHubRepoLicense?,
        val allowForking: Boolean,
        val isTemplate: Boolean,
        val webCommitSignoffRequired: Boolean,
        val topics: List<String>,
        val visibility: String,
        val forks: Int,
        val openIssues: Int,
        val watchers: Int,
        val defaultBranch: String,
        val tempCloneToken: String?,
        val networkCount: Int,
        val subscribersCount: Int
    )

    data class GitHubRepoRecord(
        val repo: GitHubRepoDetail,
        val pullRequests: List<GitHubPullRequestRecord>,
    )

}