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
data class OpenAiEmbeddingResponse(
    @JsonProperty("object")
    val objectType: String,
    val data: List<OpenAiEmbedding>,
    val model: String,
    val usage: Usage,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Usage(
        val promptTokens: Int,
        val totalTokens: Int,
    )
}

