package com.arbr.platform.object_graph.artifact

abstract class DomainArtifact<DomainRoot, A : DomainRoot>(
    val domainArtifactRootClass: Class<DomainRoot>,
) : Artifact {

    abstract fun innerArtifact(): A
}

