package com.arbr.og_engine.artifact.processor.base

import com.arbr.engine.services.workflow.model.WorkflowState
import com.arbr.og_engine.artifact.processor.impl.OrphanArtifactQueueDelegate

fun interface WorkflowStateArtifactProcessorFactory {

    fun makeArtifactProcessor(
        orphanArtifactQueueDelegate: OrphanArtifactQueueDelegate,
    ): ArtifactProcessor<WorkflowState>
}