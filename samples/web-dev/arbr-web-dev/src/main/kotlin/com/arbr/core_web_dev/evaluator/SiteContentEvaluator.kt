package com.arbr.core_web_dev.evaluator

import com.arbr.api_server_base.service.git.client.GitAgentHotReloadWebserverClient
import com.arbr.api_server_base.service.git.client.HotReloadWebserverProcessClient
import com.arbr.object_model.functions.internal.code_eval.SiteContentBuildProposal
import com.arbr.og_engine.file_system.HeadlessDocumentResult
import org.slf4j.LoggerFactory
import org.springframework.context.Lifecycle
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal

/**
 * An evaluator which evaluates the content of the remote dev server.
 */
abstract class SiteContentEvaluator<P : SiteContentBuildProposal, T>(
    private val gitWebserverBaseUrl: String
): Lifecycle {
    private var hotReloadWebserverProcessClient: HotReloadWebserverProcessClient? = null
    private var headlessBrowserProcessClient: HotReloadWebserverProcessClient? = null

    abstract val defaultFailureEvaluation: T

    abstract fun evaluatePageContent(
        state: P,
        signal: Signal<HeadlessDocumentResult>
    ): Mono<T>

    fun evaluate(state: P): Mono<T> {
        // Warning! NPM web apps only
        val volumeState = state.volumeState
        val hotReloadClient = GitAgentHotReloadWebserverClient(volumeState.workflowHandleId, gitWebserverBaseUrl)

        return hotReloadClient.getOrStartHotReloadServer(
            state.projectName
        )
            .flatMap { (hotReload, headless) ->
                hotReloadWebserverProcessClient = hotReload
                headlessBrowserProcessClient = headless

                hotReload.getDOM(volumeState, "").materialize()
            }
            .flatMap { pageContentSignal ->
                if (pageContentSignal.isOnNext) {
                    logger.info("Successfully connected to hot reload server")
                }

                evaluatePageContent(state, pageContentSignal)
            }
            .onErrorResume {
                Mono.justOrEmpty(defaultFailureEvaluation)
            }
    }

    override fun start() {
        //
    }

    override fun stop() {
        hotReloadWebserverProcessClient?.close()
        headlessBrowserProcessClient?.close()

        hotReloadWebserverProcessClient = null
        headlessBrowserProcessClient = null
    }

    override fun isRunning(): Boolean {
        return (hotReloadWebserverProcessClient != null || headlessBrowserProcessClient != null)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SiteContentEvaluator::class.java)
    }
}
