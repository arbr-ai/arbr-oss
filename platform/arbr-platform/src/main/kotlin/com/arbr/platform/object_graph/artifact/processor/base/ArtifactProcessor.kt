package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.Artifact
import reactor.core.publisher.Mono

fun interface ArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: Artifact, input: T): Mono<T>
}

