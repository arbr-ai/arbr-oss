package com.arbr.api_server_base.app

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsProcessor
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange

abstract class ExplanatoryCorsProcessor(
    private val onSuccess: ((exchange: ServerWebExchange) -> Unit)?,
    private val onFailure: ((exchange: ServerWebExchange, reason: String) -> Unit)?,
) : CorsProcessor {

    sealed class Result {
        data object Success: Result()
        data class Failure(val explanation: String): Result()
    }

    protected abstract fun processVerbose(config: CorsConfiguration?, exchange: ServerWebExchange): Result

    private fun handleResult(exchange: ServerWebExchange, result: Result) {
        when (result) {
            is Result.Failure -> onFailure?.let { f -> f(exchange, result.explanation) }
            Result.Success -> onSuccess?.let { f -> f(exchange) }
        }
    }

    override fun process(config: CorsConfiguration?, exchange: ServerWebExchange): Boolean {
        val result = processVerbose(config, exchange)
        handleResult(exchange, result)
        return result is Result.Success
    }

    companion object {
        fun withHandlers(
            onSuccess: ((exchange: ServerWebExchange) -> Unit)? = null,
            onFailure: ((exchange: ServerWebExchange, reason: String) -> Unit)? = null,
        ): ExplanatoryCorsProcessor {
            return ExplanatoryCorsProcessorImpl(onSuccess, onFailure)
        }
    }

    /**
     * Duplicate of [org.springframework.web.cors.reactive.DefaultCorsProcessor] with externalized rejection info
     */
    private class ExplanatoryCorsProcessorImpl(
        onSuccess: ((exchange: ServerWebExchange) -> Unit)? = null,
        onFailure: ((exchange: ServerWebExchange, reason: String) -> Unit)? = null,
    ) : ExplanatoryCorsProcessor(
        onSuccess, onFailure
    ) {

        private val VARY_HEADERS = listOf(
            HttpHeaders.ORIGIN, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
        )

        override fun processVerbose(
            config: CorsConfiguration?,
            exchange: ServerWebExchange
        ): Result {
            val request = exchange.request
            val response = exchange.response
            val responseHeaders = response.headers
            val varyHeaders = responseHeaders[HttpHeaders.VARY]
            if (varyHeaders == null) {
                responseHeaders.addAll(HttpHeaders.VARY, VARY_HEADERS)
            } else {
                for (header in VARY_HEADERS) {
                    if (!varyHeaders.contains(header)) {
                        responseHeaders.add(HttpHeaders.VARY, header)
                    }
                }
            }
            if (!CorsUtils.isCorsRequest(request)) {
                return Result.Success
            }
            if (responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) != null) {
                logger.trace("Skip: response already contains \"Access-Control-Allow-Origin\"")
                return Result.Success
            }
            val preFlightRequest = CorsUtils.isPreFlightRequest(request)
            return config?.let { handleInternal(exchange, it, preFlightRequest) }
                ?: if (preFlightRequest) {
                    Result.Failure(
                        "Pre-flight request missing configuration"
                    )
                } else {
                    Result.Success
                }
        }

        /**
         * Invoked when one of the CORS checks failed.
         */
        private fun rejectRequest(response: ServerHttpResponse) {
            response.statusCode = HttpStatus.FORBIDDEN
        }

        /**
         * Handle the given request.
         */
        private fun handleInternal(
            exchange: ServerWebExchange,
            config: CorsConfiguration, preFlightRequest: Boolean
        ): Result {
            val request = exchange.request
            val response = exchange.response
            val responseHeaders = response.headers
            val requestOrigin = request.headers.origin
            val allowOrigin = checkOrigin(config, requestOrigin)
                ?: return Result.Failure("Reject: '$requestOrigin' origin is not allowed")
            val requestMethod = getMethodToUse(request, preFlightRequest)
            val allowMethods = checkMethods(config, requestMethod)
                ?: return Result.Failure("Reject: HTTP '$requestMethod' is not allowed")
            val requestHeaders = getHeadersToUse(request, preFlightRequest)
            val allowHeaders = checkHeaders(config, requestHeaders)
            if (preFlightRequest && allowHeaders == null) {
                return Result.Failure("Reject: headers '$requestHeaders' are not allowed")
            }
            responseHeaders.accessControlAllowOrigin = allowOrigin
            if (preFlightRequest) {
                responseHeaders.accessControlAllowMethods = allowMethods
            }
            if (preFlightRequest && !allowHeaders!!.isEmpty()) {
                responseHeaders.accessControlAllowHeaders = allowHeaders
            }

            val exposedHeaders = config.exposedHeaders
            if (!exposedHeaders.isNullOrEmpty()) {
                responseHeaders.accessControlExposeHeaders = exposedHeaders
            }

            if (java.lang.Boolean.TRUE == config.allowCredentials) {
                responseHeaders.accessControlAllowCredentials = true
            }
            if (java.lang.Boolean.TRUE == config.allowPrivateNetwork &&
                java.lang.Boolean.parseBoolean(request.headers.getFirst(ACCESS_CONTROL_REQUEST_PRIVATE_NETWORK))
            ) {
                responseHeaders[ACCESS_CONTROL_ALLOW_PRIVATE_NETWORK] = java.lang.Boolean.toString(true)
            }

            val maxAge = config.maxAge
            if (preFlightRequest && maxAge != null) {
                responseHeaders.accessControlMaxAge = maxAge
            }

            return Result.Success
        }

        /**
         * Check the origin and determine the origin for the response. The default
         * implementation simply delegates to
         * [CorsConfiguration.checkOrigin].
         */
        private fun checkOrigin(config: CorsConfiguration, @Nullable requestOrigin: String?): String? {
            return config.checkOrigin(requestOrigin)
        }

        /**
         * Check the HTTP method and determine the methods for the response of a
         * pre-flight request. The default implementation simply delegates to
         * [CorsConfiguration.checkHttpMethod].
         */
        private fun checkMethods(config: CorsConfiguration, @Nullable requestMethod: HttpMethod?): List<HttpMethod>? {
            return config.checkHttpMethod(requestMethod)
        }

        private fun getMethodToUse(request: ServerHttpRequest, isPreFlight: Boolean): HttpMethod? {
            return if (isPreFlight) request.headers.accessControlRequestMethod else request.method
        }

        /**
         * Check the headers and determine the headers for the response of a
         * pre-flight request. The default implementation simply delegates to
         * [CorsConfiguration.checkHeaders].
         */
        private fun checkHeaders(config: CorsConfiguration, requestHeaders: List<String>?): List<String>? {
            return config.checkHeaders(requestHeaders)
        }

        private fun getHeadersToUse(request: ServerHttpRequest, isPreFlight: Boolean): List<String> {
            val headers = request.headers
            return if (isPreFlight) headers.accessControlRequestHeaders else ArrayList(headers.keys)
        }


        companion object {
            private val logger = LoggerFactory.getLogger(ExplanatoryCorsProcessor::class.java)

            /**
             * The `Access-Control-Request-Private-Network` request header field name.
             * @see [Private Network Access specification](https://wicg.github.io/private-network-access/)
             */
            private const val ACCESS_CONTROL_REQUEST_PRIVATE_NETWORK = "Access-Control-Request-Private-Network"

            /**
             * The `Access-Control-Allow-Private-Network` response header field name.
             * @see [Private Network Access specification](https://wicg.github.io/private-network-access/)
             */
            private const val ACCESS_CONTROL_ALLOW_PRIVATE_NETWORK = "Access-Control-Allow-Private-Network"
        }
    }
}