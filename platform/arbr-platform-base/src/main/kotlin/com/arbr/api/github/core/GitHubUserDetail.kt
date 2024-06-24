package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUserDetail(
    val id: Long,
    val login: String?,
    val avatarUrl: String?,
    val emails: List<GitHubUserEmail>,
    val organizations: List<GitHubOrganizationInfo>,
    val repos: List<GitHubRepoDetail>,
)
