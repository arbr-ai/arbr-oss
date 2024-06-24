package com.arbr.api_server_base.service.engine.impl.hypervisor

import com.arbr.api_server_base.service.engine.base.EngineSpawner
import com.arbr.api_server_base.service.github.GitHubCredentialsUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class EngineHypervisorClient(
    private val host: String,
): EngineSpawner {

    private val webClient: WebClient = run {
        WebClient
            .builder()
            .baseUrl(host)
            .filter { request, next ->
                next.exchange(request)
                    .doOnSubscribe {
                        logger.info("${request.method()} ${request.url()}")
                    }
                    .doOnNext { resp ->
                        logger.info("${resp.statusCode()} ${request.method()} ${request.url()}")
                    }
            }
            .build()
    }

    override fun spawnEngineWorker(): Mono<Void> {
        return GitHubCredentialsUtils
            .fromContext()
            .flatMap { credentials ->
                webClient
                    .post()
                    .uri("spawn")
                    .header(HttpHeaders.AUTHORIZATION, "token ${credentials.accessToken}")
                    .bodyValue(Unit)
                    .retrieve()
                    .bodyToMono(Unit::class.java)
            }
            .then()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EngineHypervisorClient::class.java)
    }

}