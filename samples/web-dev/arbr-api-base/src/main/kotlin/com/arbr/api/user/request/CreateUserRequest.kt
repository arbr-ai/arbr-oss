package com.arbr.api.user.request

import com.arbr.api.github.request.GitHubCreateUserRequest
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserRequest(
    val credentials: CredentialsBasedCreateUserRequest?,
    val gitHub: GitHubCreateUserRequest?
)
