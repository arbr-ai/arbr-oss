package com.arbr.relational_prompting.services.ai_application.config

class AiApplicationConfigFactoryImpl(
    private val baseConfig: AiApplicationBaseConfig,
): AiApplicationConfigFactory {
    private val defaultCompletionModelFilter = CompletionModelFilter.default()

    override fun builder(
        applicationId: String,
    ): AiApplicationConfigFactory.Companion.Builder0 = AiApplicationConfigFactory.Companion.Builder0(
        baseConfig,
        defaultCompletionModelFilter,
        applicationId,
    )
}