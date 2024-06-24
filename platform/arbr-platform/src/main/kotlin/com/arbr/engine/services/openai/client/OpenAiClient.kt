package com.arbr.engine.services.openai.client

import com.arbr.content_formats.tokens.TokenizationUtils
import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.completions.config.OpenAiConfig
import com.arbr.relational_prompting.generics.EmbeddingProvider
import com.arbr.relational_prompting.generics.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

@Component
class OpenAiClient(
    private val config: OpenAiConfig,
    private val mapper: ObjectMapper,
) : ChatCompletionProvider, EmbeddingProvider {
    private val logger = LoggerFactory.getLogger(OpenAiClient::class.java)

    private val retryPolicy = Retry
        .backoff(10L, Duration.ofSeconds(5L))
        .filter {
            if (it is WebClientResponseException) {
                it.statusCode != HttpStatus.BAD_REQUEST
            } else {
                true
            }
        }
        .doBeforeRetry {
            val failure = it.failure()
            if (failure is WebClientResponseException) {
                val statusCode = failure.statusCode.value()
                logger.info("Rate limited ($statusCode) by OpenAI (${it.totalRetries()}), waiting...")
            } else {
                logger.warn("Retriable error from OpenAI (${it.totalRetries()})", failure)
            }
        }

    private val webClients = ConcurrentHashMap<Long, WebClient>()

    @PostConstruct
    fun init() {
        latencyLogger.info("application_id,model,prompt_tokens,completion_tokens,elapsed_ms")
    }

    private fun getWebClient(targetTimeoutMs: Long): WebClient {
        return webClients.computeIfAbsent(targetTimeoutMs) {
            val timeout = Duration.ofMillis(targetTimeoutMs)
            val httpClient = HttpClient.create()
                .responseTimeout(timeout)
                .resolver {
                    it.queryTimeout(timeout)
                }
                .doOnConnected { conn ->
                    conn
                        .addHandlerFirst(ReadTimeoutHandler(timeout.toSeconds().toInt()))
                        .addHandlerFirst(WriteTimeoutHandler(timeout.toSeconds().toInt()))
                }

            val strategies = ExchangeStrategies.builder()
                .codecs { codecs: ClientCodecConfigurer ->
                    codecs.defaultCodecs().maxInMemorySize(maxRequestSize)
                }
                .build()

            WebClient
                .builder()
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${config.apiKey}")
                .baseUrl("${config.baseUrl}/${config.apiVersion}")
                .filter(
                    ExchangeFilterFunction.ofRequestProcessor { req ->
                        logger.debug("{} {}", req.method(), req.url())
                        Mono.just(req)
                    }
                )
                .exchangeStrategies(strategies)
                .build()
        }
    }

    private fun getChatCompletionWebClient(
        model: OpenAiChatCompletionModel,
        maxTokens: Long,
    ): WebClient {
        // Expected value multiplier
        val expectedMultiplier = when (model) {
            OpenAiChatCompletionModel.GPT_3_5_TURBO,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_0613,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_1106,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_0125 -> 28

            OpenAiChatCompletionModel.GPT_3_5_TURBO_16K,
            OpenAiChatCompletionModel.GPT_3_5_TURBO_16K_0613 -> 38

            OpenAiChatCompletionModel.GPT_4_0314,
            OpenAiChatCompletionModel.GPT_4_0613,
            OpenAiChatCompletionModel.GPT_4_1106_PREVIEW,
            OpenAiChatCompletionModel.GPT_4_0125_PREVIEW,
            OpenAiChatCompletionModel.GPT_4 -> 120
        }

        // Multiplier for timeout
        val toleranceFactor = 2L

        val latencyBucket =
            (maxTokens * expectedMultiplier * toleranceFactor).div(5000) * 5000 // Round to nearest 5 seconds
        val latencyBucketBounded = min(defaultLatencyHighestMs, max(latencyBucket, defaultLatencyLowMs))

        return getWebClient(latencyBucketBounded)
    }

    override fun getChatCompletion(
        applicationId: String,
        request: OpenAiChatCompletionRequest,
    ): Mono<OpenAiChatCompletion> {
        return getChatCompletionWebClient(
            request.model,
            request.maxTokens?.toLong() ?: 1024,
        )
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OpenAiChatCompletion::class.java)
            .retryWhen(retryPolicy)
            .doOnSubscribe {
                logger.info("Input: ${mapper.writeValueAsString(request)}")
            }
            .doOnNext {
                logger.info("Output: ${mapper.writeValueAsString(it)}")
            }
            .doOnError(WebClientResponseException::class.java) {
                logger.info("Error from OpenAI Chat Completions request")
                logger.info("Status:${it.statusCode}")
                logger.info("Error Body:\n${it.responseBodyAsString}")
                logger.info("----")
                logger.info("Request:\n${mapper.writeValueAsString(request)}")
                logger.info("Prompt length: ${TokenizationUtils.getTokenCount(request.messages.joinToString("\n") { m -> m.content })}")
                logger.info("====")
            }
            .timed()
            .map {
                val elapsedMs = it.elapsed().toMillis()
                val result = it.get()
                latencyLogger.info("$applicationId,${result.model},${result.usage.promptTokens},${result.usage.completionTokens},$elapsedMs")

                result
            }
    }

    override fun getEmbedding(
        request: OpenAiEmbeddingRequest
    ): Mono<OpenAiEmbeddingResponse> {
        return getWebClient(defaultLatencyLowMs)
            .post()
            .uri("/embeddings")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OpenAiEmbeddingResponse::class.java)
            .retryWhen(retryPolicy)
            .doOnError(DecodingException::class.java) {
                logger.info("Decoding error from OpenAI Embedding request")
                logger.info("----")
                logger.info("Request:\n${mapper.writeValueAsString(request)}")
                logger.info("Prompt length: ${request.input.maxOfOrNull { i -> TokenizationUtils.getTokenCount(i) }}")
                logger.info("====")
            }
            .doOnError(WebClientResponseException::class.java) {
                logger.info("Error from OpenAI Embedding request")
                logger.info("Status:${it.statusCode}")
                logger.info("Error Body:\n${it.responseBodyAsString}")
                logger.info("----")
                logger.info("Request:\n${mapper.writeValueAsString(request)}")
                logger.info("Prompt length: ${request.input.maxOfOrNull { i -> TokenizationUtils.getTokenCount(i) }}")
                logger.info("====")
            }
    }

    companion object {
        private const val maxRequestSize = 16 * 1024 * 1024

        private const val defaultLatencyLowMs = 4000L
        private const val defaultLatencyHighMs = 240000L
        private const val defaultLatencyHighestMs = 480000L

        private val latencyLogger = LoggerFactory.getLogger("com.arbr.engine.app.config.logging.LatencyLogger")
    }
}
