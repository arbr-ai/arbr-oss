package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.WorkflowResourceUpdateArtifact
import reactor.core.publisher.Mono

fun interface WorkflowResourceUpdateArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: WorkflowResourceUpdateArtifact, input: T): Mono<T>
}