package com.arbr.api.user_repos.response

import com.arbr.api.github.core.GitHubRepoInfo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetUserReposResponse(
    val reposByOwner: Map<String, List<GitHubRepoInfo>>,
)
