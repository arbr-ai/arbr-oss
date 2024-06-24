package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Basic GitHub user info for plaintext API consumption.
 * Do not include private info.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUserInfo(
    val id: Long,
    val login: String?,
    val avatarUrl: String?,
)
