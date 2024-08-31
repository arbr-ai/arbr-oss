package com.arbr.platform.object_graph.artifact.processor.base

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.platform.object_graph.artifact.processor.impl.OrphanArtifactQueueDelegate

fun interface WorkflowStateArtifactProcessorFactory {

    fun makeArtifactProcessor(
        orphanArtifactQueueDelegate: OrphanArtifactQueueDelegate,
    ): ArtifactProcessor<WorkflowState>
}