package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.relational_prompting.services.ai_application.application.PromptShortener
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel

data class AiApplicationConfigModel<I : SourcedStruct, O : SourcedStruct>(
    val numExamplesToIncludeInPrompt: Long? = null,
    val hardcodedExamples: List<Pair<I, O>>? = null,
    val promptShortener: PromptShortener<I>? = null,
    val chatMessagePreProcessor: ((ChatMessage) -> ChatMessage)? = null,
    val maxTokens: Int? = null,
    val model: OpenAiChatCompletionModel? = null,
)
