package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.Artifact
import reactor.core.publisher.Mono

fun interface ArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: Artifact, input: T): Mono<T>
}

