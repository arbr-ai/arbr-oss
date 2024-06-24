package com.arbr.core_web_dev.evaluator

import com.arbr.object_model.functions.internal.code_eval.BuildFeedback
import com.arbr.og_engine.file_system.ShellOutput
import com.arbr.og_engine.file_system.VolumeState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * An evaluator which decides whether a proposed VolumeState builds successfully.
 */
@Component
class BuildEvaluator(
    @Value("\${arbr.build-evaluator.fail-on-error-logged:false}")
    private val npmFailOnErrorOutput: Boolean,
) {
    private fun installDependencies(
        projectName: String,
        volumeState: VolumeState,
    ): Mono<BuildFeedback> {
        return volumeState.execWriteInProject("npm install")
            .map {
                it.grep(
                    Regex("npm ERR!"),
                    contextLines = 10,
                    segmentDivider = "...",
                )
            }
            .map { shout ->
                val success = shout.status == 0 && shout.stdout.isEmpty() && shout.stderr.isEmpty()
                val logMessage = if (success) {
                    "Install succeeded"
                } else {
                    "Install failed:\n" + shout.stdout.joinToString("\n") + "\nStderr:\n" + shout.stderr.joinToString("\n")
                }
                logger.info(logMessage)

                if (success) {
                    BuildFeedback(true, emptyList())
                } else {
                    BuildFeedback(
                        false,
                        listOf(
                            shout.stdout.joinToString("\n"),
                            shout.stderr.joinToString("\n")
                        ).filter { it.isNotBlank() })
                }
            }
    }

    private fun isSuccess(
        shellOutput: ShellOutput
    ): Boolean {
        return if (npmFailOnErrorOutput) {
            shellOutput.status == 0 && shellOutput.stdout.isEmpty() && shellOutput.stderr.isEmpty()
        } else {
            shellOutput.status == 0
        }
    }

    private fun build(
        projectName: String,
        volumeState: VolumeState,
    ): Mono<BuildFeedback> {
        return volumeState.execWriteInProject("npm run build")
            .map {
                it.grep(
                    Regex("[Ee]rror"),
                    contextLines = 10,
                    segmentDivider = "...",
                )
            }
            .map { shout ->
                val success = isSuccess(shout)
                val logMessage = if (success) {
                    "Build succeeded"
                } else {
                    "Build failed:\n" + shout.stdout.joinToString("\n") + "\nStderr:\n" + shout.stderr.joinToString("\n")
                }
                logger.info(logMessage)

                if (success) {
                    BuildFeedback(true, emptyList())
                } else {
                    BuildFeedback(
                        false,
                        listOf(
                            shout.stdout.joinToString("\n"),
                            shout.stderr.joinToString("\n")
                        ).filter { it.isNotBlank() })
                }
            }
    }

    fun evaluate(
        projectName: String,
        volumeState: VolumeState,
    ): Mono<BuildFeedback> {
        return installDependencies(projectName, volumeState)
            .flatMap { installEval ->
                if (installEval.success) {
                    build(projectName, volumeState)
                } else {
                    Mono.just(installEval)
                }
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BuildEvaluator::class.java)
    }
}
