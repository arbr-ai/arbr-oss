package com.arbr.object_model.processor.config

import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.resource.ArbrCommit
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.object_model.functions.external.github.GitHubProjectFunctions
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.WorkflowCommitArtifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Successful commit -> commit and yield hash
 */
@Component
@ConditionalOnProperty(
    prefix = "arbr.processor.commit-writing",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class WorkflowCommitWritingProcessor(
    objectModelParser: ObjectModelParser,
    private val gitHubProjectFunctions: GitHubProjectFunctions,
) : ArbrResourceFunction<
        ArbrCommit, PartialCommit,
        ArbrTask, PartialTask,
        ArbrCommit, PartialCommit,
        >(objectModelParser) {
    override val name: String
        get() = "commit-writing"

    override val targetResourceClass: Class<ArbrTask>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrCommit>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrCommit,
        readResource: ArbrTask,
        writeResource: ArbrCommit,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrCommit, PartialCommit, ArbrForeignKey>) -> Mono<Void> {
        if (writeResource.commitHash.getLatestValue() != null) {
            // Already committed
            throw OperationCompleteException()
        }

        val allCommits = require(readResource.subtasks).flatMap { (_, st) ->
            require(st.commits).values
        }

        // Check that all prior commits are committed
        for (priorCommit in allCommits) {
            if (priorCommit.uuid == writeResource.uuid) {
                // Reached this commit
                break
            }

            require(priorCommit.commitHash)
        }

        val commitMessage = require(writeResource.commitMessage)

        val fileOps = require(writeResource.fileOps)

        // Require a proposed implementation of each file op, as a reference to the implemented file version
        fileOps.forEach { (_, fileOp) ->
            require(fileOp.fileOperation)
            require(fileOp.targetFile)
            require(fileOp.implementedFile)
        }

        // Accept completion evals that are either complete or mostly complete, plus a passing build.
        requireThatValue(writeResource.commitEvals) { completionStatusList ->
            completionStatusList.any {
                (it.value.complete.getLatestAcceptedValue()?.value == true || it.value.mostlyComplete.getLatestAcceptedValue()?.value == true) && (it.value.buildSuccess.getLatestAcceptedValue()?.value == true)
            }
        }

        val branchName = require(readResource.branchName)

        return { partialObjectGraph ->
            val writeCommit = partialObjectGraph.root



            gitHubProjectFunctions.commitAndGetCommitEventData(
                volumeState,
                commitMessage,
                writeCommit.diffSummary,
            ).flatMap { eventData ->
                // Commit URL technically isn't valid until the commit is pushed, but seems overly-cautious to wait
                artifactSink.next(
                    WorkflowCommitArtifact(
                        commitHash = eventData.commitHash,
                        commitMessage = eventData.commitMessage,
                        commitUrl = eventData.commitUrl,
                        diffSummary = eventData.diffSummary,
                        diffStat = eventData.diffStat,
                    )
                )

                gitHubProjectFunctions.pushUpstreamOrigin(volumeState, branchName.value)
                    .then(
                        Mono.defer {
                            writeCommit.commitHash =
                                ArbrCommit.CommitHash.materialized(eventData.commitHash)

                            Mono.empty()
                        }
                    )
            }
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrCommit,
        readTargetResource: ArbrTask,
        writeTargetResource: ArbrCommit,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        requireLatest(listenResource.commitHash)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowCommitWritingProcessor::class.java)
    }
}