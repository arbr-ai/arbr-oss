package com.arbr.platform.object_graph.core

import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.file_system.VolumeState
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