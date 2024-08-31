package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.ProcessorStatusArtifact
import reactor.core.publisher.Mono

fun interface ProcessorStatusArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: ProcessorStatusArtifact, input: T): Mono<T>
}

