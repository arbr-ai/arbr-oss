package com.arbr.engine.services.completions.factory

import com.arbr.content_formats.mapper.Mappers
import com.arbr.engine.services.completions.config.OpenAiConfig
import com.arbr.engine.services.completions.provider.OpenAiCompletionProvider
import com.arbr.engine.services.openai.client.OpenAiClient

class OpenAiCompletionProviderFactory: CompletionProviderFactory<OpenAiConfig, OpenAiCompletionProvider> {
    override val configClass: Class<OpenAiConfig> = OpenAiConfig::class.java

    override fun makeProvider(config: OpenAiConfig): OpenAiCompletionProvider {
        val openAiClient = OpenAiClient(
            config,
            mapper,
        )
        return OpenAiCompletionProvider(openAiClient)
    }

    companion object {
        private val mapper = Mappers.mapper
    }
}
