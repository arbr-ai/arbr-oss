package com.arbr.platform.object_graph.artifact.processor.impl

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.platform.object_graph.artifact.ProcessorStatusArtifact
import com.arbr.platform.object_graph.artifact.processor.base.ProcessorStatusArtifactProcessor
import reactor.core.publisher.Mono

class DefaultProcessorStatusArtifactProcessor : ProcessorStatusArtifactProcessor<WorkflowState> {
    override fun processArtifact(artifact: ProcessorStatusArtifact, input: WorkflowState): Mono<WorkflowState> {
        // No-op
        return Mono.empty()
    }
}

