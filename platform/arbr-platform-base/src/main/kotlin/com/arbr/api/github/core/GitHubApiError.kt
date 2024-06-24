package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubApiInnerError(
    val resource: String?,
    val code: String?,
    val field: String?,
    val message: String?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubApiError(
    val message: String?,
    val errors: List<GitHubApiInnerError>?,
    val documentationUrl: String?,
) {
    private fun anyErrorMatches(resource: String?, message: String?): Boolean {
        return errors?.any {
            it.resource == resource
                    && it.message == message
        } ?: false
    }

    fun isExactly(resource: String?, message: String?): Boolean {
        return errors?.size == 1 && anyErrorMatches(resource, message)
    }

}
