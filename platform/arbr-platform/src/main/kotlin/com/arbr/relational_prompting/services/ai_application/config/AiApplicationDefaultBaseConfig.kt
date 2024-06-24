package com.arbr.relational_prompting.services.ai_application.config

class AiApplicationDefaultBaseConfig: AiApplicationBaseConfig by AiApplicationBaseConfigImpl(
    CompletionModelFilter.default(),
    0.2,
)
