package com.arbr.engine.services.completions.factory

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.completions.base.DelegatingChatCompletionProvider
import com.arbr.engine.services.completions.base.DelegatingChatCompletionProviderImpl

class DelegatingChatCompletionProviderFactory(
    private val providers: List<ChatCompletionProvider>,
) {
    fun makeDelegatingChatCompletionProvider(): DelegatingChatCompletionProvider {
        return DelegatingChatCompletionProviderImpl(providers)
    }
}