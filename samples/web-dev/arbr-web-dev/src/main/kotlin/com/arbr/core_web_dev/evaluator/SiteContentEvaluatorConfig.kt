package com.arbr.core_web_dev.evaluator

import com.arbr.object_model.functions.internal.code_eval.BuildFeedback
import com.arbr.object_model.functions.internal.code_eval.SiteContentBasicBuildProposal
import com.arbr.og_engine.file_system.HeadlessDocumentResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal

@Configuration
class SiteContentEvaluatorConfig(
    @Value("\${topdown.git.base_url}")
    private val gitWebserverBaseUrl: String,
) {

    private val prefixes = listOf(
        "ERROR: ",
        "CONSOLE: ",
        "RESP: ",
        "REQUEST FAILED: ",
    )

    private fun parseErrors(lines: List<String>): List<String> {
        val allErrors = mutableListOf<String>()

        var prefix: String? = null
        val currentError = mutableListOf<String>()
        for (line in lines) {
            val linePrefix = prefixes.firstOrNull { line.startsWith(it) }
            if (linePrefix != null) {
                prefix = linePrefix

                if (currentError.isNotEmpty()) {
                    allErrors.add(currentError.joinToString("\n"))
                    currentError.clear()
                }

                if (linePrefix == "ERROR: ") {
                    val plainLine = line.drop(prefix.length)
                    currentError.add(plainLine)
                }
            } else {
                if (prefix == "ERROR: ") {
                    currentError.add(line)
                }
            }
        }

        if (currentError.isNotEmpty()) {
            allErrors.add(currentError.joinToString("\n"))
        }

        return allErrors
    }

    @Bean("hotReloadEvaluator")
    fun hotReloadEvaluator(): SiteContentEvaluator<SiteContentBasicBuildProposal, BuildFeedback> =
        object : SiteContentEvaluator<SiteContentBasicBuildProposal, BuildFeedback>(gitWebserverBaseUrl) {
            override val defaultFailureEvaluation: BuildFeedback = BuildFeedback(
                false,
                emptyList(),
            )

            override fun evaluatePageContent(
                state: SiteContentBasicBuildProposal,
                signal: Signal<HeadlessDocumentResult>
            ): Mono<BuildFeedback> {
                val eval = if (signal.isOnNext) {
                    val documentContentResult = signal.get()!!

                    val errors = documentContentResult.output.filter { it["kind"] == "pageerror" }
                        .map { it["message"] as String }

                    if (errors.isEmpty()) {
                        BuildFeedback(true, emptyList())
                    } else {
                        BuildFeedback(false, errors)
                    }
                } else {
                    logger.info("Failed to get headless browser result")
                    // TODO: Handle
                    BuildFeedback(true, emptyList())
                }

                return Mono.just(eval)
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(SiteContentEvaluatorConfig::class.java)
    }
}
