package com.arbr.engine.services.workflow.state

import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.file_system.VolumeState
import com.arbr.platform.object_graph.core.WorkflowResourceModel
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