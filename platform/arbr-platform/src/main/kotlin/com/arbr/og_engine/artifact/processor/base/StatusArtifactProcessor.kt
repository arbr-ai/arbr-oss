package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.StatusArtifact
import reactor.core.publisher.Mono

fun interface StatusArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: StatusArtifact, input: T): Mono<T>
}