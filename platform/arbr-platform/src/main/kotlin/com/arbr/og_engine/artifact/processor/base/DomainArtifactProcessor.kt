package com.arbr.og_engine.artifact.processor.base

import com.arbr.og_engine.artifact.DomainArtifact
import reactor.core.publisher.Mono

interface DomainArtifactProcessor<DomainRoot, A: DomainRoot, T : Any> {

    /**
     * Return whether the processor accepts the artifact rooted in `DomainRoot`.
     */
    fun canProcessArtifact(artifact: DomainArtifact<*, *>): Boolean

    /**
     * Process the artifact asynchronously, transforming a value.
     */
    fun processArtifact(artifact: DomainArtifact<*, *>, input: T): Mono<T>
}
