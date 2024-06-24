package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * GitHub Organization info from API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubOrganizationMembershipInfo(
    val organization: GitHubOrganizationInfo,
)
