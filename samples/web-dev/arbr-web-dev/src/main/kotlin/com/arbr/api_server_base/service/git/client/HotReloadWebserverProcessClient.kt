package com.arbr.api_server_base.service.git.client

import com.arbr.og_engine.file_system.HeadlessDocumentResult
import com.arbr.og_engine.file_system.ShellOutput
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.reactor.fireAndForget
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.net.URL
import java.time.Duration
import java.util.concurrent.TimeoutException

class HotReloadWebserverProcessClient(
    private val interiorFlux: Flux<ShellOutput>,
    private val connectionTimeout: Duration = Duration.ofSeconds(12L)
) {
    class DisconnectedException : Exception("Not connected to hot reload webserver")

    private var hypervisorSink: FluxSink<Unit>? = null
    private val hypervisor = Flux.create<Unit> { sink ->
        hypervisorSink = sink
    }.share().cache()

    private var baseUrl: String? = null
    private var webClient: WebClient? = null

    fun initiate(): Mono<Void> {
        var connectionSink: MonoSink<Unit>? = null
        var connected = false
        val connectionMono = Mono.create<Unit> { sink ->
            connectionSink = sink
            if (connected) {
                sink.success(Unit)
            }
        }
            .timeout(connectionTimeout)
            .onErrorMap(TimeoutException::class.java) { DisconnectedException() }
            .doOnNext {
                logger.info("Resolved address of hot reload server")
            }
            .doOnError(DisconnectedException::class.java) {
                logger.error("Failed to connect to hot reload server")
            }

        val opMono = interiorFlux
            .doOnNext { shout ->
                if (webClient == null) {
                    val match = shout.stdout.firstNotNullOfOrNull { localhostPortRegex.find(it) }
                    val port = match?.groups?.get(1)?.value?.toIntOrNull()
                    if (port != null) {
                        val newBaseUrl = "http://localhost:$port/"
                        webClient = WebClient.builder()
                            .baseUrl(newBaseUrl)
                            .build()
                        baseUrl = newBaseUrl
                        connected = true
                        connectionSink?.success(Unit)
                    }
                }
            }
            .onErrorResume {
                logger.info("Closing remote webserver connection - ${it::class.java.simpleName}")

                Mono.empty()
            }
            .collectList()
            .thenReturn(Unit)

        val hypervisorMono = hypervisor
            .collectList()
            .thenReturn(Unit)

        return Flux.merge(opMono, hypervisorMono)
            .next()
            .fireAndForget(
                Schedulers.boundedElastic()
            )
            .then(connectionMono)
            .then()
    }

    fun get(href: String): Mono<String> {
        val webClient = webClient
            ?: return Mono.error(DisconnectedException())

        return webClient
            .get()
            .uri(href)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun getDOM(
        volumeState: VolumeState,
        href: String
    ): Mono<HeadlessDocumentResult> {
        val baseUrl = baseUrl
            ?: return Mono.error(DisconnectedException())

        val path = URL(URL(baseUrl), href).toString()

        return volumeState.pingHeadless(path)
    }

    fun close() {
        hypervisorSink!!.next(Unit)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HotReloadWebserverProcessClient::class.java)

        private val localhostPortRegex = Regex("http://localhost:(\\d+)/")
    }
}