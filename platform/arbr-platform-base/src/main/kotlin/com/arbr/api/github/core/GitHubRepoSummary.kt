package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * TODO: Deduplicate with GitHubRepoDetail
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubRepoSummary(
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

    /**
     * The default branch for the repo.
     */
    val defaultBranch: String,
)
