package com.arbr.test_util

import com.arbr.data_common.base.DataRecordObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class CodeEditDiffAlignmentTestCase(
    val cacheKey: String,
    val model: String,
    val filePath: String,
    val baseDocument: String,
    val rawModelOutput: String,
    val parsedCodeBlocks: List<String>,
    val alignedDocument: String,
): DataRecordObject
