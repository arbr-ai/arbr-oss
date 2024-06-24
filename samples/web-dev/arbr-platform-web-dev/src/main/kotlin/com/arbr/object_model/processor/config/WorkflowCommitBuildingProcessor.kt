package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialCommitEval
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.internal.code_eval.WebDevBuildEvalFunctions
import com.arbr.object_model.functions.internal.code_eval.WebDevSiteContentEvalFunctions
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Successful commit eval -> build eval -> actually commit and yield hash
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.commit-building", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowCommitBuildingProcessor(
    objectModelParser: ObjectModelParser,
    private val webDevBuildEvalFunctions: WebDevBuildEvalFunctions,
    private val webDevSiteContentEvalFunctions: WebDevSiteContentEvalFunctions,
    @Value("\${topdown.processors.commit_max_repair_attempts:4}")
    private val maxRepairAttempts: Int,
) : ArbrResourceFunction<
        ArbrCommitEval, PartialCommitEval,
        ArbrProject, PartialProject,
        ArbrCommitEval, PartialCommitEval,
        >(objectModelParser) {
    override val name: String
        get() = "commit-building"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrCommitEval>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrCommitEval,
        readResource: ArbrProject,
        writeResource: ArbrCommitEval,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrCommitEval, PartialCommitEval, ArbrForeignKey>) -> Mono<Void> {
        val tasks = require(readResource.tasks).values
        val subtasks = tasks.flatMap { require(it.subtasks).values }
        val allCommits = subtasks.flatMap { subtask ->
            require(subtask.commits).values
        }

        val thisCommit = requireAttachedOrElseComplete(listenResource.parent)

        if (thisCommit.commitHash.getLatestAcceptedValue() != null) {
            // Already committed
            return { partialObjectGraph ->
                partialObjectGraph.root.buildSuccess = ArbrCommitEval.BuildSuccess.constant(true)
                partialObjectGraph.root.renderSuccess = ArbrCommitEval.RenderSuccess.constant(true)
                partialObjectGraph.root.errorContent = ArbrCommitEval.ErrorContent.constant(null)
                Mono.empty()
            }
        }

        // Check that all prior commits are committed
        for (priorCommit in allCommits) {
            if (priorCommit.uuid == thisCommit.uuid) {
                // Reached this commit
                break
            }

            require(priorCommit.commitHash)
        }

        val numAttempts = thisCommit.commitEvals.items.getLatestAcceptedValue()?.size ?: 0

        return { partialObjectGraph ->
            val completionStatus = partialObjectGraph.root

            logger.info("Building for commit completion status ${completionStatus.uuid} - ${thisCommit.commitMessage.getLatestAcceptedValue()}")

            val projectName = require(readResource.fullName)

            if (numAttempts > maxRepairAttempts) {
                // Pass the build due to attempts exhausted - move on and hope that subsequent changes address the issue
                logger.warn("Repair attempts exhausted on commit ${thisCommit.uuid} - moving on")
                completionStatus.buildSuccess = ArbrCommitEval.BuildSuccess.constant(true)
                completionStatus.renderSuccess = ArbrCommitEval.RenderSuccess.constant(true)
                completionStatus.errorContent = ArbrCommitEval.ErrorContent.constant("") // Could be null?
                Mono.empty()
            } else {
                webDevBuildEvalFunctions.evaluateNpmBuild(
                    projectName.value,
                    volumeState,
                ).flatMap { evaluation ->
                    // TODO: Decide the best way to handle multiple errors
                    val errorContent = evaluation.failures.joinToString("\n\n") { it.trim() }
                    val buildSuccess = evaluation.success

                    logger.info("Reporting build ${if (buildSuccess) "success" else "failure"} on commit: ${thisCommit.commitMessage.getLatestAcceptedValue()}")
                    if (errorContent.isNotBlank()) {
                        logger.info("Build failures:\n$errorContent")
                    }

                    completionStatus.buildSuccess = ArbrCommitEval.BuildSuccess.materialized(
                        buildSuccess
                    )
                    completionStatus.errorContent = ArbrCommitEval.ErrorContent.materialized(
                        errorContent
                    )

                    if (buildSuccess) {
                        // Attempt headless render only if build succeeds
                        webDevSiteContentEvalFunctions.evaluatePageLoad(
                            projectName.value,
                            volumeState,
                            "",
                        )
                            .map { hotReloadEvaluation ->
                                // TODO: Decide the best way to handle multiple errors
                                val hotReloadErrorContent =
                                    hotReloadEvaluation.failures.joinToString("\n\n") { it.trim() }
                                val renderSuccess = evaluation.success

                                logger.info("Reporting render ${if (renderSuccess) "success" else "failure"} on commit: ${thisCommit.commitMessage.getLatestAcceptedValue()?.value}")
                                if (hotReloadErrorContent.isNotBlank()) {
                                    logger.info("Render failures:\n$hotReloadErrorContent")
                                }

                                completionStatus.renderSuccess = ArbrCommitEval.RenderSuccess.materialized(
                                    renderSuccess
                                )
                                // Build was successful, so overwrite error content regardless
                                completionStatus.errorContent = ArbrCommitEval.ErrorContent.materialized(
                                    hotReloadErrorContent
                                )
                            }
                    } else {
                        // If the build fails, mark the render a failure to proceed with repair
                        completionStatus.renderSuccess = ArbrCommitEval.RenderSuccess.materialized(
                            false
                        )

                        Mono.empty()
                    }.then()
                }
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrCommitEval,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrCommitEval,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        requireLatest(listenResource.buildSuccess)
        requireLatest(listenResource.errorContent)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowCommitBuildingProcessor::class.java)
    }
}