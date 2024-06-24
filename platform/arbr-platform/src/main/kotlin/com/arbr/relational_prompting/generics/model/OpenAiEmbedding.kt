package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * https://platform.openai.com/docs/api-reference/embeddings/create
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OpenAiEmbedding(
    @JsonProperty("object")
    val objectType: String,
    val embedding: List<Double>,
    val index: Int,
)