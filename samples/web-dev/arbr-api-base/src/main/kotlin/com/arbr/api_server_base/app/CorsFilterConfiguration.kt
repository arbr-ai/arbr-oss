package com.arbr.api_server_base.app

import com.arbr.content_formats.mapper.Mappers
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsProcessor
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.WebFilter

@Configuration
class CorsFilterConfiguration(
    @Value("\${topdown.cors.log-rejections:true}")
    private val logCorsRejections: Boolean,
) {
    private val corsConfigurationValue: CorsConfiguration = CorsConfiguration().also {
        it.allowedOrigins = listOf(
            "http://127.0.0.1:3000/",
            "http://127.0.0.1:5173/",
            "http://127.0.0.1/",
            "http://localhost:3000/",
            "http://localhost:5173/",
            "http://localhost/",
            "https://www.tpdn.ai/",
            "https://tpdn.ai/",
            "https://api.tpdn.ai/",
            "https://www.arbr.ai/",
            "https://arbr.ai/",
            "https://api.arbr.ai/",
        )
        it.allowCredentials = true
        it.allowedMethods = listOf("*")
        it.allowedHeaders = listOf("*")
    }

    @Bean
    fun corsConfiguration(): CorsConfiguration {
        return corsConfigurationValue
    }

    @Bean
    fun corsProcessor(): CorsProcessor {
        return ExplanatoryCorsProcessor.withHandlers(
            onFailure = { exchange, reason ->
                if (logCorsRejections) {
                    val loggerMap = RequestContext(exchange, null).toLoggerMap()
                    logger.info("Rejecting CORS: $reason\nRequest context: ${mapper.writeValueAsString(loggerMap)}")
                }
            }
        )
    }

    @Bean
    fun corsWebFilter(
        corsProcessor: CorsProcessor,
    ): WebFilter {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfigurationValue)

        return CorsWebFilter(source, corsProcessor)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CorsFilterConfiguration::class.java)
        private val mapper = Mappers.mapper

    }
}
