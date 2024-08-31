package com.arbr.platform.object_graph.artifact


sealed class WorkflowArtifact : DomainArtifact<
        WorkflowArtifact, WorkflowArtifact
        >(
    WorkflowArtifact::class.java
) {

    override fun innerArtifact(): WorkflowArtifact {
        return this
    }
}
