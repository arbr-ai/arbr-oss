package com.arbr.api_server_base.service.git.client

import com.arbr.api_server_base.service.github.GitHubCredentialsUtils
import com.arbr.og_engine.file_system.HeadlessDocumentResult
import com.arbr.og_engine.file_system.ShellOutput
import com.arbr.util_common.reactor.single
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

class GitWebClient(
    private val workflowHandleId: String,
    private val baseUrl: String,
) {

    private val webClient: WebClient = run {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(120L))
            .resolver {
                it.queryTimeout(Duration.ofSeconds(120L))
            }

        val strategies = ExchangeStrategies.builder()
            .codecs { codecs: ClientCodecConfigurer ->
                codecs.defaultCodecs().maxInMemorySize(maxRequestSize)
            }
            .build()

        WebClient
            .builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(workflowHeader, workflowHandleId)
            .filter { request, next ->
                GitHubCredentialsUtils.fromContext()
                    .map { it.accessToken }
                    .single("Not logged into GitHub")
                    .flatMap { accessToken ->
                        next.exchange(
                            ClientRequest.from(request)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                                .build()
                        )
                    }
            }
            .filter { request, next ->
                next.exchange(request)
                    .doOnNext { resp ->
                        if (!resp.statusCode().is2xxSuccessful) {
                            logger.info("${resp.statusCode()} ${request.method()} ${request.url()}")
                        }
                    }
            }
            .exchangeStrategies(strategies)
            .build()
    }

    fun clone(
        projectFullName: String,
    ): Mono<Void> {
        return webClient
            .post()
            .uri("/clone")
            .header(projectHeader, projectFullName)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .then()
    }

    fun copyFromStarter(
        projectFullName: String,
        starterId: String
    ): Mono<Void> {
        return webClient
            .put()
            .uri("/copy-from-starter")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(projectHeader, projectFullName)
            .bodyValue(starterId)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .then()
    }

    fun pingHeadless(
        projectFullName: String,
        targetUrl: String,
    ): Mono<HeadlessDocumentResult> {
        return webClient
            .get()
            .uri { builder ->
                builder
                    .path("/headless/browse")
                    .queryParam("targetUrl", targetUrl)
                    .build()
            }
            .header(projectHeader, projectFullName)
            .retrieve()
            .bodyToMono(HeadlessDocumentResult::class.java)
    }

    fun execInProject(
        projectFullName: String,
        cmd: String,
    ): Mono<ShellOutput> {
        return webClient
            .post()
            .uri("/sh")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(projectHeader, projectFullName)
            .bodyValue(cmd)
            .retrieve()
            .bodyToMono(ShellOutput::class.java)
            .doOnNext {
                logger.debug(it.toString())
            }
    }

    fun read(
        projectFullName: String,
        fileRelativePath: String,
    ): Mono<String> {
        return webClient
            .get()
            .uri("/file")
            .header(projectHeader, projectFullName)
            .header(fileHeader, fileRelativePath)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun touch(
        projectFullName: String,
        fileRelativePath: String,
    ): Mono<Void> {
        return webClient
            .put()
            .uri("/file/touch")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(projectHeader, projectFullName)
            .header(fileHeader, fileRelativePath)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .then()
    }

    fun write(
        projectFullName: String,
        fileRelativePath: String,
        contents: String,
    ): Mono<Void> {
        return webClient
            .put()
            .uri("/file")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(projectHeader, projectFullName)
            .header(fileHeader, fileRelativePath)
            .bodyValue(contents)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .then()
    }

    fun delete(
        projectFullName: String,
        fileRelativePath: String,
    ): Mono<Void> {
        return webClient
            .delete()
            .uri("/file")
            .header(projectHeader, projectFullName)
            .header(fileHeader, fileRelativePath)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .then()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GitWebClient::class.java)

        private const val workflowHeader = "X-Topdown-Workflow-Id"
        private const val projectHeader = "X-GitHub-Project"
        private const val fileHeader = "X-GitHub-File"
        private const val maxRequestSize = 16 * 1024 * 1024
    }
}