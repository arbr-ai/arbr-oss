package com.arbr.engine.services.workflow.state

import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

interface WorkflowInitializationService {

    /**
     * Begin sending update notifications via the root resource.
     */
    fun beginUpdates(
        workflowResourceModel: WorkflowResourceModel,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Void>

    fun createWorkflowResourceModel(
        userId: Long,
        workflowHandleId: String,
        projectFullName: String,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        preloadFromWorkflowHandleId: Long?,
    ): WorkflowResourceModel

}
