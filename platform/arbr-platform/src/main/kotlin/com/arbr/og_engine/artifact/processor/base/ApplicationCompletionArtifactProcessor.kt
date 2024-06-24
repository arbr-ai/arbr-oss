package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.ApplicationCompletionArtifact
import reactor.core.publisher.Mono

fun interface ApplicationCompletionArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: ApplicationCompletionArtifact, input: T): Mono<T>
}