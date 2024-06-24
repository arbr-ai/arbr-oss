package com.arbr.api.user.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CredentialsBasedLoginRequest(
    val userId: Long?,
    val username: String?,
    val email: String?,
    val password: String,
)