package com.arbr.engine.services.completions.config

data class OpenAiConfig(
    val baseUrl: String,
    val apiKey: String,
    val apiVersion: String
): CompletionProviderConfig()

