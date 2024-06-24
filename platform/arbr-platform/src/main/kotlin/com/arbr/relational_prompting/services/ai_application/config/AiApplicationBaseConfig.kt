package com.arbr.relational_prompting.services.ai_application.config

/**
 * Config independent of any single application
 */
interface AiApplicationBaseConfig {
    val completionModelFilter: CompletionModelFilter
    val topP: Double
}