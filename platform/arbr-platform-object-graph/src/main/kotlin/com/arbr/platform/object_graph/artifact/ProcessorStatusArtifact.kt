package com.arbr.og_engine.artifact

import com.arbr.platform.object_graph.artifact.WorkflowProcessorStatus

/**
 * Artifact signaling a status change in a processor.
 */
data class ProcessorStatusArtifact(
    val processorName: String,
    val resourceUuid: String,
    val status: WorkflowProcessorStatus,
): Artifact