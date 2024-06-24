package com.arbr.og_engine.artifact.processor.impl

import com.arbr.engine.services.user.WorkflowResourceRepository
import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.WorkflowResourceUpdateArtifact
import com.arbr.og_engine.artifact.processor.base.WorkflowResourceUpdateArtifactProcessor
import reactor.core.publisher.Mono

class DefaultWorkflowResourceUpdateArtifactProcessor(
    private val workflowResourceRepository: WorkflowResourceRepository,
) : WorkflowResourceUpdateArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: WorkflowResourceUpdateArtifact, input: WorkflowState): Mono<WorkflowState> {
        val resourceData = artifact.resourceData
            .entries
            .filter { it.value != null }
            .associate { it.key to it.value!! }

        return workflowResourceRepository
            .updateResource(artifact.objectModelUuid, resourceData)
            .then(Mono.empty())
    }
}
