package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiApplicationFactoryConfig(
    private val applicationExampleProvider: ApplicationExampleProvider,
    private val applicationCompletionCache: ApplicationCompletionCache,
    private val chatCompletionProvider: ChatCompletionProvider,
    @Value("\${topdown.openai.enable-gpt4:true}")
    private val enableGpt4: Boolean,
    @Value("\${topdown.openai.top-p}")
    private val configTopP: Double?,
) {
    private val defaultBaseConfig = AiApplicationDefaultBaseConfig()
    private val baseConfig = AiApplicationBaseConfigImpl(
        completionModelFilter(enableGpt4),
        configTopP ?: defaultBaseConfig.topP,
    )

    @Bean
    fun aiApplicationFactory(): AiApplicationFactory {
        return AiApplicationFactoryImpl(
            applicationExampleProvider,
            applicationCompletionCache,
            chatCompletionProvider,
            baseConfig,
        )
    }

    companion object {
        private fun completionModelFilter(
            enableGpt4: Boolean
        ): CompletionModelFilter {
            return CompletionModelFilter { model ->
                enableGpt4 || model !in listOf(
                    OpenAiChatCompletionModel.GPT_4_1106_PREVIEW,
                    OpenAiChatCompletionModel.GPT_4_0613,
                    OpenAiChatCompletionModel.GPT_4_0314,
                    OpenAiChatCompletionModel.GPT_4
                )
            }
        }
    }
}