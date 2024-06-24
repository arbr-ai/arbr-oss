package com.arbr.api_server_base.app

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Component
class WarmupRoutine {

    private val webClient = WebClient.builder().build()

    fun warmup(): Mono<Void> {
        return webClient
            .get()
            .uri("https://www.google.com/")
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnSubscribe {
                logger.info("Warmup: pinging google")
            }
            .doOnNext {
                logger.info("Warmup: got ${it.length} bytes")
            }
            .doOnError {
                logger.error("Warmup: got error")
            }
            .then()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WarmupRoutine::class.java)
    }
}
