package com.arbr.engine.services.completions.provider

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.openai.client.OpenAiClient
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletion
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import reactor.core.publisher.Mono

class OpenAiCompletionProvider(
    private val openAiClient: OpenAiClient,
): ChatCompletionProvider {
    override fun getChatCompletion(
        applicationId: String,
        request: OpenAiChatCompletionRequest
    ): Mono<OpenAiChatCompletion> {
        return openAiClient.getChatCompletion(
            applicationId,
            request,
        )
    }
}