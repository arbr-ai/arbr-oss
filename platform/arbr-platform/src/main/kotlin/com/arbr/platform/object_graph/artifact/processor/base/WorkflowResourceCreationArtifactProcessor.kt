package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.WorkflowResourceCreationArtifact
import reactor.core.publisher.Mono

fun interface WorkflowResourceCreationArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: WorkflowResourceCreationArtifact, input: T): Mono<T>
}