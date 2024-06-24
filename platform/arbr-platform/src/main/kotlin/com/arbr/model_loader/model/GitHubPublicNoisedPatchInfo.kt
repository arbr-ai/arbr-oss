package com.arbr.model_loader.model

import com.arbr.data_common.base.DataRecordObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubPublicNoisedPatchInfo(
    val repoId: Int,
    val repoFullName: String,
    val pullRequestId: Long,
    val commitSha: String,
    val fileSha: String,
    val fileContent: String,
    val patchContent: String,
    val baseDocument: String,
    val patchContentWithNoise: String,
    val noiseAddedDifficulty: NoiseAddedDifficulty,
    val noiseModelVersion: String,
): DataRecordObject
