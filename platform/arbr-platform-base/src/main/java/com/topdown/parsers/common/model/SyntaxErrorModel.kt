package com.topdown.parsers.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Simple representation intended for direct de/serialization.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SyntaxErrorModel(
    val offendingToken: TokenModel?,
    val line: Int,
    val charPositionInLine: Int,
    // Index of error within content globally
    val locus: Int?,
    val message: String?,
    val exception: RecognitionExceptionModel?,
)
