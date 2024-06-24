package com.arbr.platform.autoconfigure.completions

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class ChatCompletionProviderAutoConfigurationTest {

    private val contextRunner: ApplicationContextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            CompletionProviderConfigurationBundleAutoConfiguration::class.java,
            OpenAiAutoConfiguration::class.java,
            ChatCompletionProviderAutoConfiguration::class.java,
        ))

    @Test
    fun `configures completion provider`() {
        contextRunner.run { context ->
            val providers = context.getBeansOfType(ChatCompletionProvider::class.java)
            providers.forEach {
                println(it)
            }
        }
    }

}