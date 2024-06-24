package com.arbr.object_model.processor.config

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.partial.PartialTask
import com.arbr.object_model.core.partial.PartialTaskEval
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.ArbrTaskEval
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.StatusArtifact
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
import java.util.*

@Component
@ConditionalOnProperty(prefix = "arbr.processor.task-status-publisher", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowTaskStatusPublishingProcessor(
    objectModelParser: ObjectModelParser,
) : ArbrResourceFunction<
        ArbrTask, PartialTask,
        ArbrTask, PartialTask,
        ArbrTask, PartialTask,
        >(objectModelParser) {
    override val name: String
        get() = "task-status-publisher"

    override val targetResourceClass: Class<ArbrTask>
        get() = cls()
    override val writeTargetResourceClass: Class<ArbrTask>
        get() = cls()

    override fun prepareUpdate(
        listenResource: ArbrTask,
        readResource: ArbrTask,
        writeResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrTask, PartialTask, ArbrForeignKey>) -> Mono<Void> {
        val isAlreadyCancelled = listenResource.taskEvals.items.getLatestValue()
            ?.any { it.value.resource()?.cancelled?.getLatestValue()?.value == true } == true
        if (isAlreadyCancelled) {
            throw OperationCompleteException()
        }

        // Right now just marks complete when a PR is opened (which implies completion)
        require(listenResource.pullRequestTitle)
        require(listenResource.pullRequestBody)
        require(listenResource.pullRequestHtmlUrl)

        return { partialObjectGraph ->
            val task = partialObjectGraph.root

            val resource = PartialTaskEval(
                partialObjectGraph,
                UUID.randomUUID().toString(),
            ).apply {
                this.parent = PartialRef(task.uuid)
                cancelled = ArbrTaskEval.Cancelled.materialized(true)
                cancelReason = ArbrTaskEval.CancelReason.materialized(0)
            }

            // "Cancel" the task indicating completion
            task.taskEvals = (task.taskEvals ?: ImmutableLinkedMap())
                .adding(
                    resource.uuid, resource
                )

            Mono.fromCallable {
                logger.info("Emitting success artifact for ${volumeState.workflowHandleId}")
                artifactSink.next(
                    StatusArtifact(
                        WorkflowStatus.SUCCEEDED,
                        value = null,
                        throwable = null,
                    )
                )
            }
                .then()
        }
    }

    override fun checkPostConditions(
        listenResource: ArbrTask,
        readTargetResource: ArbrTask,
        writeTargetResource: ArbrTask,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        val isAlreadyCancelled = listenResource.taskEvals.items.getLatestAcceptedValue()
            ?.any { it.value.resource()?.cancelled?.getLatestAcceptedValue()?.value == true } == true
        if (isAlreadyCancelled) {
            return
        }

        requireLatestChildren(listenResource.taskEvals.items)

        requireThatLatest(listenResource.taskEvals.items) { childMap ->
            childMap.values.any {
                it.resource()?.cancelled?.getLatestValue()?.value == true
            }
        }

        requireLatest(listenResource.pullRequestTitle)
        requireLatest(listenResource.pullRequestBody)
        requireLatest(listenResource.pullRequestHtmlUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowTaskStatusPublishingProcessor::class.java)
    }
}
