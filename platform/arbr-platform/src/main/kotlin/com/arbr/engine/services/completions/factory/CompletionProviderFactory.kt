package com.arbr.engine.services.completions.factory

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.completions.config.CompletionProviderConfig

interface CompletionProviderFactory<C: CompletionProviderConfig, P: ChatCompletionProvider> {
    val configClass: Class<C>

    fun makeProvider(config: C): P
}