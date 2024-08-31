package com.arbr.engine.services.workflow.state

import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.core.WorkflowResourceModel
import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

interface WorkflowInitializingResourceManager {
    fun createWorkflowResourceModel(
        userId: Long,
        workflowHandleId: String,
        projectFullName: String,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        preloadFromWorkflowHandleId: Long?,
    ): Mono<WorkflowResourceModel>
}