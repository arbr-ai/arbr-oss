package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * TODO: Deduplicate across projects
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SegmentIndexReprTreeNode(
    val contentType: SegmentContentType,
    val ruleName: String,
    val name: String?,
    val elementIndex: Int,
    val startIndex: Int,
    val endIndex: Int,
    val content: String,
)
