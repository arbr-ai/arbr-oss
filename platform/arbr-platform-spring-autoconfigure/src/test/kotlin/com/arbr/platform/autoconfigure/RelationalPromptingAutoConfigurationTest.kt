package com.arbr.platform.autoconfigure

import com.arbr.platform.autoconfigure.relational_prompting.RelationalPromptingAutoConfiguration
import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner


class RelationalPromptingAutoConfigurationTest {

    private val contextRunner: ApplicationContextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(RelationalPromptingAutoConfiguration::class.java))

    @Test
    fun configures() {
        contextRunner.run { context ->
            val cache = context.getBean(ApplicationCompletionCache::class.java)
            println(cache)
        }
    }

}
