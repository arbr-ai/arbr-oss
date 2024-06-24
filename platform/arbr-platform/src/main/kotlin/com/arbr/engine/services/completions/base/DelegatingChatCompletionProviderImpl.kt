package com.arbr.engine.services.completions.base

import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionRequest

internal class DelegatingChatCompletionProviderImpl(
    private val providers: List<ChatCompletionProvider>,
): DelegatingChatCompletionProvider() {
    override fun selectProvider(
        applicationId: String,
        request: OpenAiChatCompletionRequest
    ): ChatCompletionProvider {
        /**
         * TODO: Do some kind of prioritization or load balancing or configurable delegation
         */
        return providers.first()
    }
}