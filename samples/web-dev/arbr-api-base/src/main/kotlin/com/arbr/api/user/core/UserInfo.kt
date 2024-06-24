package com.arbr.api.user.core

import com.arbr.api.github.core.GitHubUserDetail
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Basic user info for plaintext API consumption.
 * Do not include private info.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserInfo(
    val id: Long,
    val username: String?,
    val avatarUrl: String?,

    /**
     * GitHub user connection, if one exists.
     */
    val gitHub: GitHubUserDetail?,
)
