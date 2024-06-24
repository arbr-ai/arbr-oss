package com.arbr.og_engine.artifact.processor.impl

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.ApplicationCompletionArtifact
import com.arbr.og_engine.artifact.processor.base.ApplicationCompletionArtifactProcessor
import reactor.core.publisher.Mono

class DefaultApplicationCompletionArtifactProcessor : ApplicationCompletionArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: ApplicationCompletionArtifact, input: WorkflowState): Mono<WorkflowState> {
        // No-op
        return Mono.empty()
    }
}