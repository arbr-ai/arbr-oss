package com.arbr.relational_prompting.generics.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * https://platform.openai.com/docs/api-reference/completions/create
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
sealed interface OpenAiCompletionRequest {
    val model: String
    val prompt: OpenAiCompletionPrompt?
    val suffix: String?
    val maxTokens: Int?
    val temperature: Double?
    val topP: Double?
    val n: Int?
    val stream: Boolean?
    val logprobs: Int?
    val echo: Boolean?
    val stop: OpenAiCompletionStop?
    val presencePenalty: Double?
    val frequencyPenalty: Double?
    val bestOf: Int?
    val logitBias: Map<String, Int>?
    val user: String?
    
    companion object {

        @JsonCreator
        @JvmStatic
        fun create(
            model: String,
            prompt: OpenAiCompletionPrompt?,
            suffix: String?,
            maxTokens: Int?,
            temperature: Double?,
            topP: Double?,
            n: Int?,
            stream: Boolean?,
            logprobs: Int?,
            echo: Boolean?,
            stop: OpenAiCompletionStop?,
            presencePenalty: Double?,
            frequencyPenalty: Double?,
            bestOf: Int?,
            logitBias: Map<String, Int>?,
            user: String?,
        ): OpenAiCompletionRequest {
            return OpenAiCompletionRequestImpl(
                model,
                prompt,
                suffix,
                maxTokens,
                temperature,
                topP,
                n,
                stream,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf,
                logitBias,
                user,
            )
        }
    }
}

/**
 * Sub-model of a completion request including parameters but not content.
 *
 * https://platform.openai.com/docs/api-reference/completions/create
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
sealed interface OpenAiCompletionModelParameters {
    val model: String
    val suffix: String?
    val maxTokens: Int?
    val temperature: Double?
    val topP: Double?
    val n: Int?
    val stream: Boolean?
    val logprobs: Int?
    val echo: Boolean?
    val stop: OpenAiCompletionStop?
    val presencePenalty: Double?
    val frequencyPenalty: Double?
    val bestOf: Int?
    val logitBias: Map<String, Int>?
    val user: String?

    fun toRequest(
        prompt: OpenAiCompletionPrompt?
    ): OpenAiCompletionRequest {
        return (this as OpenAiCompletionRequestImpl).copy(prompt = prompt)
    }

    companion object {

        @JsonCreator
        @JvmStatic
        fun create(
            model: String,
            suffix: String?,
            maxTokens: Int?,
            temperature: Double?,
            topP: Double?,
            n: Int?,
            stream: Boolean?,
            logprobs: Int?,
            echo: Boolean?,
            stop: OpenAiCompletionStop?,
            presencePenalty: Double?,
            frequencyPenalty: Double?,
            bestOf: Int?,
            logitBias: Map<String, Int>?,
            user: String?,
        ): OpenAiCompletionRequest {
            return OpenAiCompletionRequestImpl(
                model,
                null,
                suffix,
                maxTokens,
                temperature,
                topP,
                n,
                stream,
                logprobs,
                echo,
                stop,
                presencePenalty,
                frequencyPenalty,
                bestOf,
                logitBias,
                user,
            )
        }
    }
}

/**
 * https://platform.openai.com/docs/api-reference/completions/create
 */
@Suppress("DataClassPrivateConstructor")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
private data class OpenAiCompletionRequestImpl(
    override val model: String,
    override val prompt: OpenAiCompletionPrompt?,
    override val suffix: String?,
    override val maxTokens: Int?,
    override val temperature: Double?,
    override val topP: Double?,
    override val n: Int?,
    override val stream: Boolean?,
    override val logprobs: Int?,
    override val echo: Boolean?,
    override val stop: OpenAiCompletionStop?,
    override val presencePenalty: Double?,
    override val frequencyPenalty: Double?,
    override val bestOf: Int?,
    override val logitBias: Map<String, Int>?,
    override val user: String?,
): OpenAiCompletionRequest, OpenAiCompletionModelParameters