package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Sub-model of a completion request including parameters but not content.
 *
 * https://platform.openai.com/docs/api-reference/completions/create
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
sealed interface OpenAiChatCompletionModelParameters {
    val model: OpenAiChatCompletionModel
    val temperature: Double?
    val topP: Double?
    val n: Int?
    val stream: Boolean?
    val stop: OpenAiChatCompletionStop?
    val maxTokens: Int?
    val presencePenalty: Double?
    val frequencyPenalty: Double?
    val logitBias: Map<String, Int>?
    val user: String?
}