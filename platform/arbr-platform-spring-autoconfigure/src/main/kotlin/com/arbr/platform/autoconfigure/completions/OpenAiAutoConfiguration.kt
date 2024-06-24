package com.arbr.platform.autoconfigure.completions

import com.arbr.engine.services.completions.config.OpenAiConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(OpenAiConfig::class)
class OpenAiAutoConfiguration(
    @Value("\${topdown.openai.base_url}")
    private val baseUrl: String,
    @Value("\${topdown.openai.api_key}")
    private val apiKey: String,
    @Value("\${topdown.openai.api_version}")
    private val apiVersion: String,
) {

    @Bean
    fun getOpenAiConfig(): OpenAiConfig {
        return OpenAiConfig(
            baseUrl,
            apiKey,
            apiVersion
        )
    }
}