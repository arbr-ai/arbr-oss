package com.arbr.platform.object_graph.artifact.processor.impl

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.platform.object_graph.artifact.ApplicationCompletionArtifact
import com.arbr.platform.object_graph.artifact.processor.base.ApplicationCompletionArtifactProcessor
import reactor.core.publisher.Mono

class DefaultApplicationCompletionArtifactProcessor : ApplicationCompletionArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: ApplicationCompletionArtifact, input: WorkflowState): Mono<WorkflowState> {
        // No-op
        return Mono.empty()
    }
}