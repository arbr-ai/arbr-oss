package com.arbr.engine.services.workflow.state

import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class DefaultWorkflowInitializingResourceManager(
    private val workflowInitializationService: WorkflowInitializationService,
    private val workflowResourceModels: ConcurrentHashMap<String, WorkflowResourceModel>,
): WorkflowInitializingResourceManager {

    private fun subscribeToUpdates(
        workflowResourceModel: WorkflowResourceModel,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>
    ): Mono<Void> {
        return workflowInitializationService.beginUpdates(
            workflowResourceModel,
            volumeState,
            artifactSink,
        )
    }

    override fun createWorkflowResourceModel(
        userId: Long,
        workflowHandleId: String,
        projectFullName: String,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
        preloadFromWorkflowHandleId: Long?
    ): Mono<WorkflowResourceModel> {
        val workflowResourceModel = workflowResourceModels.computeIfAbsent(workflowHandleId) {
            workflowInitializationService.createWorkflowResourceModel(
                userId,
                workflowHandleId,
                projectFullName,
                volumeState,
                artifactSink,
                preloadFromWorkflowHandleId,
            )
        }

        return subscribeToUpdates(
            workflowResourceModel,
            volumeState,
            artifactSink
        ).thenReturn(workflowResourceModel)
    }

}