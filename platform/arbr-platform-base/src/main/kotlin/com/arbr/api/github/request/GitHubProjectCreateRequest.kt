package com.arbr.api.github.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubProjectCreateRequest(
    val organizationName: String,
    val repositoryName: String,
    val projectDescription: String,
)
