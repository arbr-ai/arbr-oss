package com.arbr.api.github.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUserOAuthLoginRequest(
    val clientId: String,
    val clientSecret: String,
    val code: String,
    val redirectUri: String?,
)
