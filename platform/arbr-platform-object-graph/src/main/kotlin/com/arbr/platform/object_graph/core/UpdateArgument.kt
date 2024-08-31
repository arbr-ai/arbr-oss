package com.arbr.platform.object_graph.core

import com.arbr.platform.object_graph.artifact.Artifact
import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.FluxSink
import reactor.util.context.ContextView

data class UpdateArgument<T>(
    val workflowId: Long,
    val resource: T,
    val volumeState: VolumeState,
    val artifactSink: FluxSink<Artifact>,
    val contextView: ContextView,
    val retriesRemaining: Int,
    val lastException: Throwable?,
)