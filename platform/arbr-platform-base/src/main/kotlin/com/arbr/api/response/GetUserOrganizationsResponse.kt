package com.arbr.api.response

import com.arbr.api.github.core.GitHubOrganizationInfo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetUserOrganizationsResponse(
    val organizationInfo: List<GitHubOrganizationInfo>,
)
