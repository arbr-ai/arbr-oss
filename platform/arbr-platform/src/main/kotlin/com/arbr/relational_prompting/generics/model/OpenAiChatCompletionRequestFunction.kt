package com.arbr.relational_prompting.generics.model

import com.arbr.content_formats.json_schematized.JsonSchema
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OpenAiChatCompletionRequestFunction(
    val name: String,
    val description: String?,
    val parameters: JsonSchema,
)
