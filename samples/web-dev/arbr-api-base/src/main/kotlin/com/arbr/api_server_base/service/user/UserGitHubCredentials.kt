package com.arbr.api_server_base.service.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserGitHubCredentials(
    /**
     * Username aka "login" for the GitHub account.
     */
    val login: String,
    val accessToken: String,
)