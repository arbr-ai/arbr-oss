package com.arbr.api_server_base.service.github.client

import com.arbr.api.github.core.GitHubApiOAuthAccessTokenRequestError
import com.arbr.api.github.request.GitHubUserOAuthLoginRequest
import com.arbr.api.github.response.GitHubUserOAuthLoginResponse
import com.arbr.api_server_base.service.github.model.GitHubApp
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.util.MimeTypeUtils
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.nio.charset.StandardCharsets
import java.time.Duration

class GitHubLoginClient(
    private val mapper: ObjectMapper,
    private val gitHubApps: List<GitHubApp>,
) {

    private val logger = LoggerFactory.getLogger(GitHubLoginClient::class.java)

    private val retryPolicy = Retry
        .backoff(10L, Duration.ofSeconds(10L))
        .filter {
            it is WebClientResponseException.TooManyRequests
                    || it is RequestNotPermitted
                    || it is WebClientResponseException.Forbidden
                    || (it is WebClientResponseException && it.statusCode.is5xxServerError)
        }
        .doBeforeRetry {
            logger.info("Rate limited by GitHub (${it.totalRetries()}), waiting...")
        }

    private val rateLimiter: RateLimiter by lazy {
        val config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .limitForPeriod(80)
            .timeoutDuration(Duration.ofSeconds(90))
            .build()

        // Create registry
        val rateLimiterRegistry = RateLimiterRegistry.of(config)

        // Use registry
        rateLimiterRegistry.rateLimiter("github")
    }

    private val webClient: WebClient = run {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(120L))
            .resolver {
                it.queryTimeout(Duration.ofSeconds(5L))
            }

        val strategies = ExchangeStrategies.builder()
            .codecs { codecs: ClientCodecConfigurer ->
                codecs.defaultCodecs().maxInMemorySize(maxRequestSize)
                codecs.customCodecs().run {
                    register(CustomCodecs.snakeCaseDecoderHttpMessageReader())
                    register(CustomCodecs.snakeCaseEncoderHttpMessageWriter())
                }
            }
            .build()

        WebClient
            .builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter { request, next ->
                next.exchange(request)
                    .doOnSubscribe {
                        logger.info("${request.method()} ${request.url()}")
                    }
                    .doOnNext { resp ->
                        logger.info("${resp.statusCode()} ${request.method()} ${request.url()}")
                    }
                    .transformDeferred(RateLimiterOperator.of(rateLimiter))
            }
            .exchangeStrategies(strategies)
            .build()
    }

    private fun decoderHttpMessageReader(): DecoderHttpMessageReader<Any> {
        return DecoderHttpMessageReader(
            Jackson2JsonDecoder(
                jacksonObjectMapper(),
                MimeTypeUtils.APPLICATION_JSON,
            )
        )
    }

    fun loginWithOAuthCode(
        code: String,
        appName: String,
    ): Mono<GitHubUserOAuthLoginResponse> {
        val app = gitHubApps.firstOrNull {
            it.name == appName
        } ?: return Mono.error(IllegalArgumentException("No matching GitHub app configured for $appName"))
        val clientId = app.clientId
        val clientSecret = app.clientSecret

        val url = "https://github.com/login/oauth/access_token"
        val request = GitHubUserOAuthLoginRequest(
            clientId,
            clientSecret,
            code,
            redirectUri = null,
        )

        return webClient
            .post()
            .uri(url)
            .bodyValue(request)
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .flatMap { responseString ->
                        try {
                            Mono.just(
                                mapper.readValue(responseString, GitHubUserOAuthLoginResponse::class.java)
                            )
                        } catch (e: Exception) {
                            try {
                                val gitHubApiError = mapper.readValue(responseString, GitHubApiOAuthAccessTokenRequestError::class.java)
                                val charset = StandardCharsets.UTF_8

                                Mono.error(
                                    WebClientResponseException.BadRequest.create(
                                        HttpStatus.BAD_REQUEST.value(),
                                        gitHubApiError.error,
                                        response.headers().asHttpHeaders(),
                                        gitHubApiError.errorDescription.toByteArray(charset),
                                        charset,
                                    )
                                )
                            } catch (ee: Exception) {
                                // Original exception
                                Mono.error(e)
                            }
                        }
                    }
            }
            .retryWhen(retryPolicy)
    }

    companion object {
        private const val maxRequestSize = 16 * 1024 * 1024
    }
}