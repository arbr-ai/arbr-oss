package com.arbr.og_engine.artifact


sealed class WorkflowArtifact : DomainArtifact<
        WorkflowArtifact, WorkflowArtifact
        >(
    WorkflowArtifact::class.java
) {

    override fun innerArtifact(): WorkflowArtifact {
        return this
    }
}
