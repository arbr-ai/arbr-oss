package com.arbr.api_server_base.app

import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfiguration(
    private val formDataReader: FormDataReader,
) : WebFluxConfigurer {
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().let { codecs ->
            codecs.maxInMemorySize(16 * 1024 * 1024)
            codecs.multipartReader(
                formDataReader
            )
        }
    }
}
