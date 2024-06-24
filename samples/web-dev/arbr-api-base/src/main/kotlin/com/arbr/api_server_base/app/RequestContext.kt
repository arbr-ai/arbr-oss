package com.arbr.api_server_base.app

import com.arbr.api_server_base.service.auth.RequestAuthenticator
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange

data class RequestContext(
    val exchange: ServerWebExchange,
    val userCredentials: RequestAuthenticator.UserCredentials?,
) {

    private fun headerMap(headers: HttpHeaders): Map<String, String> {
        return headers
            .mapKeys { it.key.lowercase() }
            .mapValues {
                if (it.key in listOf("authorization", "cookie")) {
                    "***"
                } else {
                    it.value.joinToString("; ")
                }
            }
            .mapKeys { "headers.${it.key}" }
    }

    private fun requestToLoggerMap(request: ServerHttpRequest): Map<String, String> {
        val loggerMap = headerMap(request.headers).toMutableMap()
        loggerMap["id"] = request.id
        loggerMap["path"] = request.path.toString()
        loggerMap["method"] = request.method.name()
        loggerMap += request.queryParams
            .mapValues { it.value.joinToString(" ") }
            .mapKeys { "query.${it.key}" }

        return loggerMap
    }

    private fun responseToLoggerMap(response: ServerHttpResponse): Map<String, String> {
        val loggerMap = headerMap(response.headers).toMutableMap()

        loggerMap["status"] = (response.statusCode?.value() ?: -1).toString()

        return loggerMap
    }

    @JsonIgnore
    fun toLoggerMap(): Map<String, String> {
        val requestMap = requestToLoggerMap(exchange.request)
        val responseMap = responseToLoggerMap(exchange.response)

        return (
                requestMap.mapKeys { "request.${it.key}" }
                        + responseMap.mapKeys { "response.${it.key}" }
                        + (
                        userCredentials?.let {
                            mapOf(
                                "user.id" to it.userId.toString(),
                                "user.username" to it.username.toString(),
                            )
                        } ?: emptyMap()
                        )
                )
    }
}
