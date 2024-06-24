package com.arbr.engine.services.completions.base

import com.arbr.relational_prompting.generics.model.OpenAiChatCompletion
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest
import reactor.core.publisher.Mono

abstract class DelegatingChatCompletionProvider: ChatCompletionProvider {

    abstract fun selectProvider(
        applicationId: String,
        request: OpenAiChatCompletionRequest,
    ): ChatCompletionProvider

    override fun getChatCompletion(
        applicationId: String,
        request: OpenAiChatCompletionRequest
    ): Mono<OpenAiChatCompletion> {
        val provider = selectProvider(applicationId, request)
        return provider.getChatCompletion(applicationId, request)
    }
}
