package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.*
import reactor.core.publisher.Mono

class CombinedArtifactProcessor<T : Any>(
    private val processorStatusArtifactProcessor: ProcessorStatusArtifactProcessor<T>,
    private val statusArtifactProcessor: StatusArtifactProcessor<T>,
    private val applicationCompletionArtifactProcessor: ApplicationCompletionArtifactProcessor<T>,
    private val workflowResourceCreationArtifactProcessor: WorkflowResourceCreationArtifactProcessor<T>,
    private val workflowResourceUpdateArtifactProcessor: WorkflowResourceUpdateArtifactProcessor<T>,
    private val domainArtifactProcessors: List<DomainArtifactProcessor<*, *, T>>,
) : ArtifactProcessor<T> {
    override fun processArtifact(artifact: Artifact, input: T): Mono<T> {
        return when (artifact) {
            is ProcessorStatusArtifact -> processorStatusArtifactProcessor.processArtifact(artifact, input)
            is StatusArtifact -> statusArtifactProcessor.processArtifact(artifact, input)
            is ApplicationCompletionArtifact -> applicationCompletionArtifactProcessor.processArtifact(artifact, input)
            is WorkflowResourceCreationArtifact -> workflowResourceCreationArtifactProcessor.processArtifact(
                artifact,
                input
            )

            is WorkflowResourceUpdateArtifact -> workflowResourceUpdateArtifactProcessor.processArtifact(
                artifact,
                input
            )

            is DomainArtifact<*, *> -> {
                // Process sequentially
                domainArtifactProcessors.fold(Mono.just(input)) { state, processor ->
                    state.flatMap {
                        processor.processArtifact(artifact, it)
                    }
                }
            }
        }
    }
}
