package com.arbr.api.github.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubLoginRequest(
    /**
     * Name of the corresponding GitHub app
     */
    val appName: String,

    /**
     * Login code from the GitHub App flow
     */
    val code: String,
)
