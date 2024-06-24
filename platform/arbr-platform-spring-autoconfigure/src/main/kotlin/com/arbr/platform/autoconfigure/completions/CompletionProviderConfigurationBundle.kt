package com.arbr.platform.autoconfigure.completions

import com.arbr.engine.services.completions.config.CompletionProviderConfig
import com.arbr.engine.services.completions.factory.CompletionProviderFactory

data class CompletionProviderConfigurationBundle(
    val completionProviderFactories: List<CompletionProviderFactory<*, *>>,
    val completionProviderConfigs: List<CompletionProviderConfig>,
)