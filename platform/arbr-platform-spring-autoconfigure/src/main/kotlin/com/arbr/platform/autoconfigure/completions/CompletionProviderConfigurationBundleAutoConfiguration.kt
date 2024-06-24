package com.arbr.platform.autoconfigure.completions

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.completions.config.CompletionProviderConfig
import com.arbr.engine.services.completions.config.OpenAiConfig
import com.arbr.engine.services.completions.factory.CompletionProviderFactory
import com.arbr.engine.services.completions.factory.DelegatingChatCompletionProviderFactory
import com.arbr.engine.services.completions.factory.OpenAiCompletionProviderFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(CompletionProviderConfigurationBundle::class)
class CompletionProviderConfigurationBundleAutoConfiguration {

    private val defaultFactories: List<CompletionProviderFactory<*, *>> = listOf(
        OpenAiCompletionProviderFactory(),
        // ... Add other providers ...
    )

    @Bean
    fun getCompletionProviderConfigurationBundle(
        openAiConfigs: List<OpenAiConfig>,
    ): CompletionProviderConfigurationBundle {
        return CompletionProviderConfigurationBundle(defaultFactories, openAiConfigs)
    }

    private fun <C : CompletionProviderConfig, P : ChatCompletionProvider> coerceFactoryAndMake(
        config: C,
        factory: CompletionProviderFactory<*, P>,
    ): P {
        @Suppress("UNCHECKED_CAST")
        factory as CompletionProviderFactory<C, P>

        return factory.makeProvider(config)
    }

    @Bean
    fun getChatCompletionProvider(
        completionProviderConfigurationBundle: CompletionProviderConfigurationBundle,
    ): ChatCompletionProvider {
        val completionProviderFactories = completionProviderConfigurationBundle.completionProviderFactories
        val completionProviderConfigs = completionProviderConfigurationBundle.completionProviderConfigs

        val innerMatchedProviders = completionProviderConfigs.flatMap { config ->
            val configClass = config::class.java
            completionProviderFactories
                .filter { factory ->
                    factory.configClass.isAssignableFrom(configClass)
                }
                .map { factory ->
                    coerceFactoryAndMake(config, factory)
                }
        }

        if (innerMatchedProviders.isEmpty()) {
            throw IllegalStateException("No configured chat completion providers")
        }

        return DelegatingChatCompletionProviderFactory(innerMatchedProviders).makeDelegatingChatCompletionProvider()
    }
}
