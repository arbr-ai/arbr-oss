package com.arbr.platform.autoconfigure.relational_prompting

import com.arbr.relational_prompting.generics.DefaultEmbeddingProvider
import com.arbr.relational_prompting.generics.EmbeddingProvider
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import com.arbr.relational_prompting.generics.application_cache.DefaultApplicationCompletionCache
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.generics.examples.DefaultApplicationExampleProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RelationalPromptingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmbeddingProvider::class)
    fun getEmbeddingProvider(): EmbeddingProvider {
        return DefaultEmbeddingProvider()
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationExampleProvider::class)
    fun getApplicationExampleProvider(): ApplicationExampleProvider {
        return DefaultApplicationExampleProvider()
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationCompletionCache::class)
    fun getApplicationCompletionCache(): ApplicationCompletionCache {
        return DefaultApplicationCompletionCache()
    }
}
