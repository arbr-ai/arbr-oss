package com.arbr.model_loader.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class FileRepoRecordPairWithPatch(
    val pullRequestId: Long,
    val commitSha: String,
    val fileSha: String,
    val fileContent: String,
    val repoRecord: GitHub.GitHubRepoRecord
)