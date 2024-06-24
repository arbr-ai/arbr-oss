package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.services.ai_application.application.AiApplication

interface AiApplicationFactory : AiApplicationConfigFactory {
    fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> makeApplication(
        config: AiApplicationConfig<InputModel, OutputModel>
    ): AiApplication<InputModel, OutputModel>
}
