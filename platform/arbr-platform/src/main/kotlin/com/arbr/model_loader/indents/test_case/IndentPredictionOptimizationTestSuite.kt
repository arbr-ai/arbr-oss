package com.arbr.model_loader.indents.test_case

import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmentContentType
import com.arbr.model_loader.model.GitHub
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
internal data class IndentPredictionOptimizationTestSuite(
    val id: String,
    val contentType: SegmentContentType,
    val repoRecord: GitHub.GitHubRepoRecord,
    /**
     * Each file among these is a test case, where we train on other files, then try to predict for that file.
     */
    val repoFiles: List<IndentPredictionOptimizationTestCaseFile>,
)