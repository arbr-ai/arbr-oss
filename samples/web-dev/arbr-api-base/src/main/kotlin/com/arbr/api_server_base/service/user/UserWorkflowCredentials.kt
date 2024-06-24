package com.arbr.api_server_base.service.user

import com.arbr.api.user.core.UserJwt
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserWorkflowCredentials(
    val arbr: UserJwt?,
    val gitHub: UserGitHubCredentials?,
)