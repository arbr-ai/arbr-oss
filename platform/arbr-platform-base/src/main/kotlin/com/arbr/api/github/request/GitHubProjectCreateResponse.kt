package com.arbr.api.github.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubProjectCreateResponse(
    val organizationName: String,
    val repositoryName: String,

    /**
     * ID of the workflow initiated to create the project.
     */
    val workflowId: Long,
)
