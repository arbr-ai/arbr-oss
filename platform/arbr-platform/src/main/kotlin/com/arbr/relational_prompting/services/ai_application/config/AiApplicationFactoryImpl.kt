package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
import com.arbr.relational_prompting.services.ai_application.application.AiApplication

internal class AiApplicationFactoryImpl(
    private val applicationExampleProvider: ApplicationExampleProvider,
    private val applicationCompletionCache: ApplicationCompletionCache,
    private val chatCompletionProvider: ChatCompletionProvider,
    private val baseConfig: AiApplicationBaseConfig,
) : AiApplicationFactory, AiApplicationConfigFactory by AiApplicationConfigFactoryImpl(baseConfig) {
    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> makeApplication(
        config: AiApplicationConfig<InputModel, OutputModel>
    ): AiApplication<InputModel, OutputModel> {
        return AiApplication(
            applicationCompletionCache,
            applicationExampleProvider,
            chatCompletionProvider,
            config,
        )
    }
}