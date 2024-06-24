package com.arbr.api_server_base.app

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ApplicationRouter(
    @Value("\${topdown.application:webserver}")
    private val applicationName: String,
    private val warmupRoutine: WarmupRoutine,
) {
    private val logger = LoggerFactory.getLogger(ApplicationRouter::class.java)

    enum class Application(val propertyName: String) {
        WEBSERVER("webserver"),
    }

    private fun thisConfiguredApplication(): Application =
        Application.values().first { it.propertyName == applicationName }

    @Bean
    fun configuredApplication(): Application = thisConfiguredApplication()

    /**
     * Run and return whether to terminate.
     */
    fun run(): Mono<Boolean> {
        return when (thisConfiguredApplication()) {
            Application.WEBSERVER -> warmupRoutine.warmup()
                .doOnSubscribe {
                    logger.info("Running webserver")
                }
                                .thenReturn(false)
        }
    }
}