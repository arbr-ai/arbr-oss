package com.arbr.api.user.response

import com.arbr.api.user.core.UserInfo
import com.arbr.api.user.core.UserJwt
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserResponse(
    val user: UserInfo,
    val jwt: UserJwt,
)
