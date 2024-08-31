package com.arbr.object_model.functions.internal.code_eval

import com.arbr.platform.object_graph.file_system.VolumeState
import reactor.core.publisher.Mono

interface WebDevBuildEvalFunctions {

    fun evaluateNpmBuild(
        projectName: String,
        volumeState: VolumeState,
    ): Mono<BuildFeedback>

}
