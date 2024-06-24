package com.arbr.relational_prompting.services.ai_application.config

internal class AiApplicationBaseConfigImpl(
    override val completionModelFilter: CompletionModelFilter,
    override val topP: Double,
): AiApplicationBaseConfig