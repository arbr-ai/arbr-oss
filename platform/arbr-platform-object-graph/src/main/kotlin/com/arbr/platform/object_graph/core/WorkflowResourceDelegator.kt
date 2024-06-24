package com.arbr.platform.object_graph.core

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.collections.OneToManyResourceMap
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.util_common.reactor.fireAndForget
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentHashMap

abstract class WorkflowResourceDelegator<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey> {
    /**
     * Map limiting duplicate subscriptions.
     * One subscription per resource UUID.
     */
    private val subscriptionMap = ConcurrentHashMap<String, Unit>()

    abstract fun processSingleUpdate(
        volumeState: VolumeState,
        workflowResourceModel: WorkflowResourceModel,
        parentResourceUuid: String?,
        streamingResource: OneToManyResourceMap<T, P, ForeignKey>,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Void>

    private fun subscribeToUpdatesInner(
        volumeState: VolumeState,
        workflowResourceModel: WorkflowResourceModel,
        parentResourceUuid: String?,
        streamingResource: OneToManyResourceMap<T, P, ForeignKey>,
        artifactSink: FluxSink<Artifact>
    ): Mono<Void> {
        return Mono.defer {
            streamingResource.items.getLatestValueFlux()
                .flatMap {
                    processSingleUpdate(
                        volumeState,
                        workflowResourceModel,
                        parentResourceUuid,
                        streamingResource,
                        artifactSink,
                    )
                }
                .collectList()
        }.fireAndForget(workflowDelegatorArtifactScheduler)
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun subscribeToUpdates(
        volumeState: VolumeState,
        workflowResourceModel: WorkflowResourceModel,
        parentResourceUuid: String?,
        streamingResource: OneToManyResourceMap<T, P, ForeignKey>,
        artifactSink: FluxSink<Artifact>
    ): Mono<Void> {
        return if (subscriptionMap.putIfAbsent(streamingResource.uuid, Unit) == null) {
            subscribeToUpdatesInner(
                volumeState,
                workflowResourceModel,
                parentResourceUuid,
                streamingResource,
                artifactSink,
            )
        } else {
            Mono.empty()
        }
    }

    companion object {
        @JvmStatic
        protected val workflowDelegatorArtifactScheduler = Schedulers.newBoundedElastic(
            4,
            100_000,
            "d-artifact",
        )
    }

}