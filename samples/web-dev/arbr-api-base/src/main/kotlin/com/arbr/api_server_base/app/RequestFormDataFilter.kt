package com.arbr.api_server_base.app

import com.arbr.api_server_base.app.FormDataReader.Companion.FORM_DATA_ALT
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestFormDataFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val mediaType = exchange.request.headers.contentType
        return if (mediaType?.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED) == true) {
            val updatedRequest =
                exchange.request.mutate().header(HttpHeaders.CONTENT_TYPE, FORM_DATA_ALT.toString()).build()
            chain.filter(exchange.mutate().request(updatedRequest).build())
        } else {
            chain.filter(exchange)
        }
    }
}
