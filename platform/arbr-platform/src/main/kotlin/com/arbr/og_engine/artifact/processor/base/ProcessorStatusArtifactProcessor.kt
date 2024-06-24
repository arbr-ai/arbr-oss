package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.ProcessorStatusArtifact
import reactor.core.publisher.Mono

fun interface ProcessorStatusArtifactProcessor<T : Any> {

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: ProcessorStatusArtifact, input: T): Mono<T>
}

