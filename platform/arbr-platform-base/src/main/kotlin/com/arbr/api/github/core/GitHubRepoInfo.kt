package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubRepoInfo(
    /**
     * The plain name of the repo.
     */
    val name: String,

    /**
     * The full name of the repo, such as `topdowntest0/bookish-octo-tribble`.
     */
    val fullName: String,

    /**
     * A description of the repo.
     */
    val description: String?,

    /**
     * The primary source code language of the repo.
     */
    @JsonProperty("language")
    val primaryLanguage: String?,

    /**
     * The repo's URL in API format, such as `https://api.github.com/repos/topdowntest0/bookish-octo-tribble`.
     */
    val url: String,

    /**
     * Repo HTML url.
     */
    val htmlUrl: String,

    /**
     * The repo's URL for HTTP transport.
     */
    val cloneUrl: String,

    /**
     * The repo's URL for SSH-based transport.
     */
    val sshUrl: String,

    /**
     * The default branch for the repo.
     */
    val defaultBranch: String,

    val createdAt: String,
    val updatedAt: String,
)
