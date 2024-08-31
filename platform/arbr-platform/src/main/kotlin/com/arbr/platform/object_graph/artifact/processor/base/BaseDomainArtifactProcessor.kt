package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.platform.object_graph.artifact.DomainArtifact
import reactor.core.publisher.Mono

abstract class BaseDomainArtifactProcessor<DomainRoot, A: DomainRoot, T : Any>(
    private val domainArtifactRootClass: Class<DomainRoot>,
) : DomainArtifactProcessor<DomainRoot, A, T> {

    protected abstract fun processRootArtifact(artifact: DomainRoot, input: T): Mono<T>

    override fun processArtifact(artifact: DomainArtifact<*, *>, input: T): Mono<T> {
        // We use our extra knowledge of the type bounds here to interpret the artifact as a subtype of the root, so
        // the implementor can simply case on the inner value's class, usually sealed
        @Suppress("UNCHECKED_CAST")
        return processRootArtifact(artifact as DomainRoot, input)
    }

    override fun canProcessArtifact(artifact: DomainArtifact<*, *>): Boolean {
        // Check root type assignable by default
        return domainArtifactRootClass.isAssignableFrom(artifact.domainArtifactRootClass)
    }
}
