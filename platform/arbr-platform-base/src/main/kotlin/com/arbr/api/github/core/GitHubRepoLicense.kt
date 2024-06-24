package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubRepoLicense(
    val key: String,
    val name: String,
)