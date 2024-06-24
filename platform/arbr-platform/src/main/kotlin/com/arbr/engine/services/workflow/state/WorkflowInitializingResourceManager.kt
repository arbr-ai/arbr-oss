package com.arbr.engine.services.workflow.state

import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
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