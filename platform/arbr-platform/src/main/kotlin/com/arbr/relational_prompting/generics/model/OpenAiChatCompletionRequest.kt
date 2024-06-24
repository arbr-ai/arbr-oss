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
sealed interface OpenAiChatCompletionRequest {
    val model: OpenAiChatCompletionModel
    val messages: List<ChatMessage>
    val functions: List<OpenAiChatCompletionRequestFunction>?
    val functionCall: Any?
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

    fun withMessages(messages: List<ChatMessage>): OpenAiChatCompletionRequest

    fun withFunctionSpec(
        functions: List<OpenAiChatCompletionRequestFunction>,
        functionCall: Any,
    ): OpenAiChatCompletionRequest

    fun withModel(model: OpenAiChatCompletionModel): OpenAiChatCompletionRequest

    fun withMaxTokens(maxTokens: Int): OpenAiChatCompletionRequest

    fun withTopP(topP: Double): OpenAiChatCompletionRequest

    companion object {

        @JsonCreator
        @JvmStatic
        fun create(
            model: OpenAiChatCompletionModel,
            messages: List<ChatMessage>,
            functions: List<OpenAiChatCompletionRequestFunction>?,
            functionCall: Any?,
            temperature: Double?,
            topP: Double?,
            n: Int?,
            stream: Boolean?,
            stop: OpenAiChatCompletionStop?,
            maxTokens: Int?,
            presencePenalty: Double?,
            frequencyPenalty: Double?,
            logitBias: Map<String, Int>?,
            user: String?,
        ): OpenAiChatCompletionRequest {
            return OpenAiChatCompletionRequestImpl(
                model,
                messages,
                functions,
                functionCall,
                temperature,
                topP,
                n,
                stream,
                stop,
                maxTokens,
                presencePenalty,
                frequencyPenalty,
                logitBias,
                user,
            )
        }
    }
}

/**
 * https://platform.openai.com/docs/api-reference/completions/create
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
private data class OpenAiChatCompletionRequestImpl(
    override val model: OpenAiChatCompletionModel,
    override val messages: List<ChatMessage>,
    override val functions: List<OpenAiChatCompletionRequestFunction>?,
    override val functionCall: Any?,
    override val temperature: Double?,
    override val topP: Double?,
    override val n: Int?,
    override val stream: Boolean?,
    override val stop: OpenAiChatCompletionStop?,
    override val maxTokens: Int?,
    override val presencePenalty: Double?,
    override val frequencyPenalty: Double?,
    override val logitBias: Map<String, Int>?,
    override val user: String?,
): OpenAiChatCompletionRequest, OpenAiChatCompletionModelParameters {
    override fun withMessages(messages: List<ChatMessage>): OpenAiChatCompletionRequest {
        return copy(messages = messages)
    }

    override fun withFunctionSpec(
        functions: List<OpenAiChatCompletionRequestFunction>,
        functionCall: Any,
    ): OpenAiChatCompletionRequest {
        return copy(
            functions = functions,
            functionCall = functionCall,
        )
    }

    override fun withModel(model: OpenAiChatCompletionModel): OpenAiChatCompletionRequest {
        return copy(model = model)
    }

    override fun withMaxTokens(maxTokens: Int): OpenAiChatCompletionRequest {
        return copy(maxTokens = maxTokens)
    }

    override fun withTopP(topP: Double): OpenAiChatCompletionRequest {
        return copy(topP = topP)
    }
}
