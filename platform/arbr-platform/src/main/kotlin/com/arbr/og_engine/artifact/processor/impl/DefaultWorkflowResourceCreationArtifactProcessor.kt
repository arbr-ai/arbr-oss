package com.arbr.og_engine.artifact.processor.impl

import com.arbr.engine.services.user.WorkflowResourceRepository
import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.artifact.WorkflowResourceCreationArtifact
import com.arbr.og_engine.artifact.processor.base.WorkflowResourceCreationArtifactProcessor
import reactor.core.publisher.Mono

fun interface OrphanArtifactQueueDelegate {

    fun addToQueue(workflowId: String, artifact: Artifact)

}

class DefaultWorkflowResourceCreationArtifactProcessor(
    private val workflowResourceRepository: WorkflowResourceRepository,
    private val artifactQueueDelegate: OrphanArtifactQueueDelegate,
) : WorkflowResourceCreationArtifactProcessor<WorkflowState> {
    override fun processArtifact(
        artifact: WorkflowResourceCreationArtifact,
        input: WorkflowState
    ): Mono<WorkflowState> {
        val workflowId = input.workflowId
        val parentObjectModelUuid = artifact.parentObjectModelUuid
        val isOrphanMono = if (parentObjectModelUuid == null) {
            Mono.just(false)
        } else {
            workflowResourceRepository.get(parentObjectModelUuid)
                .map { false }
                .defaultIfEmpty(true)
        }

        val resourceData = artifact.resourceData
            .entries
            .filter { it.value != null }
            .associate { it.key to it.value!! }

        return isOrphanMono.flatMap { isOrphan ->
            if (isOrphan) {
                artifactQueueDelegate.addToQueue(
                    workflowId, artifact,
                )
                Mono.empty()
            } else {
                workflowResourceRepository.upsertResource(
                    workflowId.toLong(),
                    artifact.objectModelUuid,
                    parentObjectModelUuid,
                    artifact.resourceType.serializedName,
                    resourceData,
                ).then()
            }
        }.then(Mono.empty())
    }
}
