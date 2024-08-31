package com.arbr.engine.services.workflow.setup

import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

fun interface WorkflowSetterUpper<InputModel> {
    fun setUp(
        workflowHandleId: String,
        workflowId: String,
        input: InputModel,
        artifactSink: FluxSink<Artifact>,
    ): Mono<VolumeState>
}
