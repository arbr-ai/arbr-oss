package com.arbr.api_server_base.app

import com.arbr.api_server_base.service.auth.RequestAuthenticator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import reactor.util.context.ContextView

@Component
class RequestContextFilter(
    private val requestAuthenticator: RequestAuthenticator
) : WebFilter {
    private fun putContext(
        exchange: ServerWebExchange,
        userCredentials: RequestAuthenticator.UserCredentials?,
        ctx: Context,
    ): Context {
        return ctx
            .put(requestContext, RequestContext(exchange, userCredentials))
    }

    private fun logRequestStarted(ctxView: ContextView): Mono<Void> {
        return Mono.empty()
    }

    private fun logRequestFinished(ctxView: ContextView): Mono<Void> {
        return Mono.empty()
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return Mono.defer {
            requestAuthenticator
                .convertAuthentication(exchange)
                .materialize()
                .flatMap { userCredentials ->
                    Mono.deferContextual { outerContext ->
                        logRequestStarted(outerContext).then(
                            chain.filter(exchange)
                                .materialize()
                                .flatMap { signal ->
                                    Mono.deferContextual { innerContext ->
                                        logRequestFinished(innerContext)
                                            .thenReturn(signal)
                                    }
                                }
                                .dematerialize<Void>()
                        )
                    }.contextWrite { ctx ->
                        putContext(exchange, userCredentials.get(), ctx)
                    }
                }
        }
                }

    companion object {
        const val requestContext = "com.request_context"
    }
}