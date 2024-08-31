package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.WorkflowResourceUpdateArtifact
import reactor.core.publisher.Mono

fun interface WorkflowResourceUpdateArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: WorkflowResourceUpdateArtifact, input: T): Mono<T>
}