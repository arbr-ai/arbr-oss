package com.arbr.prompt_library.config

import com.arbr.prompt_library.PromptLibrary
import com.arbr.relational_prompting.services.ai_application.config.AiApplicationFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PromptLibraryConfig(
    private val aiApplicationFactory: AiApplicationFactory,
) {

    @Bean
    fun promptLibrary(): PromptLibrary {
        return PromptLibrary(
            aiApplicationFactory,
        )
    }

}
