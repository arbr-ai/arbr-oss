package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.fasterxml.jackson.annotation.JsonIgnore
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
data class SegmentIndexReprTree(
    val contentType: SegmentContentType,
    val ruleName: String,
    val name: String?,
    val elementIndex: Int,
    val startIndex: Int,
    val endIndex: Int,
    val content: String,
    val childElements: List<SegmentIndexReprTree>,
) {

    @JsonIgnore
    fun toNode(): SegmentIndexReprTreeNode {
        return SegmentIndexReprTreeNode(
            contentType,
            ruleName,
            name,
            elementIndex,
            startIndex,
            endIndex,
            content,
        )
    }
}
