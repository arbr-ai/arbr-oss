package com.arbr.og_engine.artifact.processor.impl

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.ProcessorStatusArtifact
import com.arbr.og_engine.artifact.processor.base.ProcessorStatusArtifactProcessor
import reactor.core.publisher.Mono

class DefaultProcessorStatusArtifactProcessor : ProcessorStatusArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: ProcessorStatusArtifact, input: WorkflowState): Mono<WorkflowState> {
        // No-op
        return Mono.empty()
    }
}

