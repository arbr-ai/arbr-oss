package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class OpenAiChatCompletion(
    val id: String,
    @JsonProperty("object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage,
) {
    enum class FinishReason(@JsonValue val reason: String) {
        LENGTH("length"),
        STOP("stop"),
        CONTENT_FILTER("content_filter"),
        FUNCTION_CALL("function_call"),
        NULL("null"),
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Choice(
        val index: Int,
        val message: ChatMessage,
        val finishReason: FinishReason = FinishReason.NULL,
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Usage(
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
    )
}
