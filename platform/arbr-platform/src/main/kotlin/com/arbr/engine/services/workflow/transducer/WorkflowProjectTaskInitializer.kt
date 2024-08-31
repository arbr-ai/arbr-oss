package com.arbr.engine.services.workflow.transducer

import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

fun interface WorkflowProjectTaskInitializer<InputModel> {
    fun initialize(
        workflowHandleId: String,
        root: ObjectModelResource<*, *, *>,
        initialVolumeState: VolumeState,
        input: InputModel,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Void>

    companion object {
        fun <T> noop(): WorkflowProjectTaskInitializer<T> =
            WorkflowProjectTaskInitializer { _, _, _, _, _ ->
                Mono.empty()
            }
    }
}
