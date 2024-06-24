package com.arbr.platform.autoconfigure.completions

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Configuration

@Configuration
//@ConditionalOnMissingBean(ChatCompletionProvider::class)
@AutoConfigureAfter(CompletionProviderConfigurationBundleAutoConfiguration::class)
class ChatCompletionProviderAutoConfiguration {

//    private fun <C : CompletionProviderConfig, P : ChatCompletionProvider> coerceFactoryAndMake(
//        config: C,
//        factory: CompletionProviderFactory<*, P>,
//    ): P {
//        @Suppress("UNCHECKED_CAST")
//        factory as CompletionProviderFactory<C, P>
//
//        return factory.makeProvider(config)
//    }
//
//    @Bean
//    fun getChatCompletionProvider(
//        completionProviderConfigurationBundle: CompletionProviderConfigurationBundle,
//    ): ChatCompletionProvider {
//        val completionProviderFactories = completionProviderConfigurationBundle.completionProviderFactories
//        val completionProviderConfigs = completionProviderConfigurationBundle.completionProviderConfigs
//
//        val innerMatchedProviders = completionProviderConfigs.flatMap { config ->
//            val configClass = config::class.java
//            completionProviderFactories
//                .filter { factory ->
//                    factory.configClass.isAssignableFrom(configClass)
//                }
//                .map { factory ->
//                    coerceFactoryAndMake(config, factory)
//                }
//        }
//
//        if (innerMatchedProviders.isEmpty()) {
//            throw IllegalStateException("No configured chat completion providers")
//        }
//
//        return DelegatingChatCompletionProviderImpl(innerMatchedProviders)
//    }
//
//    private class DelegatingChatCompletionProviderImpl(
//        private val providers: List<ChatCompletionProvider>,
//    ): DelegatingChatCompletionProvider() {
//        override fun selectProvider(
//            applicationId: String,
//            request: OpenAiChatCompletionRequest
//        ): ChatCompletionProvider {
//            /**
//             * TODO: Do some kind of prioritization or load balancing or configurable delegation
//             */
//            return providers.first()
//        }
//    }
}
