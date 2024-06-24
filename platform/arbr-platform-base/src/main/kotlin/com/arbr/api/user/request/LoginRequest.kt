package com.arbr.api.user.request

import com.arbr.api.github.request.GitHubLoginRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginRequest(
    val credentials: CredentialsBasedLoginRequest?,
    val gitHub: GitHubLoginRequest?
)
