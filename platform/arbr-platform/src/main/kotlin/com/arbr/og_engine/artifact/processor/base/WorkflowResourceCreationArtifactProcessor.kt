package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.WorkflowResourceCreationArtifact
import reactor.core.publisher.Mono

fun interface WorkflowResourceCreationArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: WorkflowResourceCreationArtifact, input: T): Mono<T>
}