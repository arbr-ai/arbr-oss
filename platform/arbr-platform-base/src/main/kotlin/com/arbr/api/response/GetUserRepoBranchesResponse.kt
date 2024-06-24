package com.arbr.api.response

import com.arbr.api.github.core.GitHubBranchSummary
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetUserRepoBranchesResponse(
    /**
     * Name of the default branch of the repo.
     */
    val defaultBranch: String,
    val branches: List<GitHubBranchSummary>,
)
