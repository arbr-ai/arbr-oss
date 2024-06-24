package com.arbr.og_engine.artifact

abstract class DomainArtifact<DomainRoot, A : DomainRoot>(
    val domainArtifactRootClass: Class<DomainRoot>,
) : Artifact {

    abstract fun innerArtifact(): A
}

