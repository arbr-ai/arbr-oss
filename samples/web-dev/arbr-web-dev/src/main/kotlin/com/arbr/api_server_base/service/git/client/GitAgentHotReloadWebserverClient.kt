package com.arbr.api_server_base.service.git.client

import com.arbr.api_server_base.service.github.GitHubCredentialsUtils
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Mildly stateful client for controlling a remote dev webserver for the workflow.
 */
class GitAgentHotReloadWebserverClient(
    private val workflowHandleId: String,
    private val baseUrl: String,
) {

    private val webClient: WebClient = run {
        val httpClient = HttpClient.create()
            .responseTimeout(streamTimeout)
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

    /**
     * Execute in project and stream partial outputs.
     * WARNING: Side-steps file read cache completely - use carefully
     */
    @Suppress("SameParameterValue")
    private fun execInProjectStream(
        projectFullName: String,
        cmd: String,
    ): Flux<ShellOutput> {
        return webClient
            .post()
            .uri("/sh_stream")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header(projectHeader, projectFullName)
            .bodyValue(cmd)
            .retrieve()
            .bodyToFlux(ShellOutput::class.java)
            .doOnEach {
                logger.debug(it.toString())
            }
    }

    @Suppress("SameParameterValue")
    private fun startHeadlessBrowserStream(
        projectFullName: String,
    ): Flux<ShellOutput> {
        return webClient
            .post()
            .uri("/headless")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header(projectHeader, projectFullName)
            .retrieve()
            .bodyToFlux(ShellOutput::class.java)
            .doOnEach {
                logger.debug(it.toString())
            }
    }

    private fun startHotReloadServerInner(
        projectFullName: String,
    ): Mono<HotReloadWebserverProcessClient> {
        // TODO: Support other npm scripts
        val interiorProcessFlux = execInProjectStream(projectFullName, "npm run dev -- --port $hotReloadPort")
            .doOnSubscribe {
                logger.info("Starting hot reload server")
            }
            .doOnNext { shout ->
                shout.stdout.forEach {
                    logger.info("[INFO] $it")
                }
                shout.stderr.forEach {
                    logger.info("[ERROR] $it")
                }
            }

        val hotReloadWebserverProcessClient = HotReloadWebserverProcessClient(interiorProcessFlux)
        return hotReloadWebserverProcessClient
            .initiate()
            .thenReturn(hotReloadWebserverProcessClient)
    }

    private fun startHeadlessServerInner(
        projectFullName: String,
    ): Mono<HotReloadWebserverProcessClient> {
        val interiorProcessFlux = startHeadlessBrowserStream(projectFullName)
            .doOnSubscribe {
                logger.info("Starting headless server")
            }
            .doOnNext { shout ->
                shout.stdout.forEach {
                    logger.info("[INFO] $it")
                }
                shout.stderr.forEach {
                    logger.info("[ERROR] $it")
                }
            }

        val hotReloadWebserverProcessClient = HotReloadWebserverProcessClient(interiorProcessFlux)
        return hotReloadWebserverProcessClient
            .initiate()
            .thenReturn(hotReloadWebserverProcessClient)
    }

    fun getOrStartHotReloadServer(
        projectFullName: String,
    ): Mono<Pair<HotReloadWebserverProcessClient, HotReloadWebserverProcessClient>> {
        val key = "$workflowHandleId-$projectFullName"
        return processClientMap.computeIfAbsent(key) {
            Mono.defer {
                startHotReloadServerInner(projectFullName)
                    .flatMap { a ->
                        startHeadlessServerInner(projectFullName)
                            .map { b -> a to b }
                    }
            }
                .cache(
                    /* ttlForValue = */
                    { Duration.ofMinutes(90L) }, // Cache values for long enough to complete a workflow
                    /* ttlForError = */
                    { Duration.ZERO }, // Do not cache errors
                    /* ttlForEmpty = */
                    { Duration.ZERO }, // Do not cache empty
                )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GitAgentHotReloadWebserverClient::class.java)

        /**
         * Static map caching clients by workflow ID.
         */
        private val processClientMap = ConcurrentHashMap<String, Mono<Pair<HotReloadWebserverProcessClient, HotReloadWebserverProcessClient>>>()

        private const val hotReloadPort = 5179

        /**
         * Max timeout to maintain a hot reload stream.
         */
        private val streamTimeout = Duration.ofHours(3L)

        private const val workflowHeader = "X-Topdown-Workflow-Id"
        private const val projectHeader = "X-GitHub-Project"
        private const val maxRequestSize = 16 * 1024 * 1024
    }
}