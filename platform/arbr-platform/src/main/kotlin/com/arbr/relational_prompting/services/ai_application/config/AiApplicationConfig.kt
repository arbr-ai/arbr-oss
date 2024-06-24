package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.relational_prompting.layers.prompt_composition.ChatMessagePreProcessor
import com.arbr.relational_prompting.layers.prompt_composition.OutputSchemaRepairFormatter
import com.arbr.relational_prompting.layers.prompt_composition.PromptComposer
import com.arbr.relational_prompting.services.ai_application.application.PromptShortener
import com.arbr.og.object_model.common.values.collections.SourcedStruct

data class AiApplicationConfig<InputModel : SourcedStruct, OutputModel : SourcedStruct>(
    val inputSchema: TemplateComponentSchema<InputModel>,
    val outputSchema: TemplateComponentSchema<OutputModel>,
    val applicationId: String,
    val promptComposer: PromptComposer,
    val numExamplesToIncludeInPrompt: Long,
    val hardcodedExamples: List<Pair<InputModel, OutputModel>>,
    val promptShortener: PromptShortener<InputModel>,
    val chatMessagePreProcessor: ChatMessagePreProcessor,
    val maxTokens: Long,
    val topP: Double,
    val model: OpenAiChatCompletionModel,
    val numOutputFormatRetries: Int,
    val numCompletionLengthRetries: Int,
    val attemptOutputSchemaRepair: Boolean,
    val outputSchemaRepairFormatter: OutputSchemaRepairFormatter,
) {
    /**
     * Models eligible for promotion when prompt length is too long even after removing examples.
     * TODO: Consider 4k 3.5 to 8k 4 promotions
     */
    val promotableModels: List<OpenAiChatCompletionModel>
        get() = when (model) {
            OpenAiChatCompletionModel.GPT_3_5_TURBO -> listOf(OpenAiChatCompletionModel.GPT_3_5_TURBO_16K)
            OpenAiChatCompletionModel.GPT_3_5_TURBO_16K -> emptyList()
            OpenAiChatCompletionModel.GPT_3_5_TURBO_0613,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_1106,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_0125 -> listOf(OpenAiChatCompletionModel.GPT_3_5_TURBO_16K_0613)
            OpenAiChatCompletionModel.GPT_3_5_TURBO_16K_0613 -> emptyList()
            OpenAiChatCompletionModel.GPT_4_0314,
            OpenAiChatCompletionModel.GPT_4_0613,
            OpenAiChatCompletionModel.GPT_4_1106_PREVIEW,
            OpenAiChatCompletionModel.GPT_4_0125_PREVIEW,
            OpenAiChatCompletionModel.GPT_4 -> emptyList()
        }

    /**
     * Default parameters for chat completion.
     * Messages are replaced in AiApplication.
     */
    val completionRequest: OpenAiChatCompletionRequest
        get() {
            return OpenAiChatCompletionRequest.create(
                model = model,
                messages = emptyList(),
                functions = null,
                functionCall = null,
                temperature = null,
                topP = topP,
                n = null,
                stream = null,
                stop = null,
                maxTokens = maxTokens.toInt(),
                presencePenalty = null,
                frequencyPenalty = null,
                logitBias = null,
                user = null,
            )
        }
}
