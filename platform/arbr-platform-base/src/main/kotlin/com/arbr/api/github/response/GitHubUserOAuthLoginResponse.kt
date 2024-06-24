package com.arbr.api.github.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUserOAuthLoginResponse(
    val accessToken: String,
    val tokenType: String, // bearer
    val scope: String,
)
