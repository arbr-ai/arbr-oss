package com.arbr.api_server_base.service.auth

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsProcessor
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class RedirectServerAuthenticationEntryPointWithCors(
    location: String,
    private val corsProcessor: CorsProcessor,
    private val corsConfiguration: CorsConfiguration,
) : RedirectServerAuthenticationEntryPoint(location) {

    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
        return super.commence(exchange, ex)
            .doOnTerminate {
                corsProcessor.process(
                    corsConfiguration,
                    exchange,
                )
            }
    }

}