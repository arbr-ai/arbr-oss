package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.ApplicationCompletionArtifact
import reactor.core.publisher.Mono

fun interface ApplicationCompletionArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: ApplicationCompletionArtifact, input: T): Mono<T>
}