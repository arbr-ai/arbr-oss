package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Repo Branch summary such as what is returned in a list by
 * GET https://api.github.com/repos/{org}/{repo}/branches
 *
 * Reference: https://docs.github.com/en/rest/branches/branches?apiVersion=2022-11-28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubBranchSummary(
    val name: String,

    // commit, protected, protection
)
