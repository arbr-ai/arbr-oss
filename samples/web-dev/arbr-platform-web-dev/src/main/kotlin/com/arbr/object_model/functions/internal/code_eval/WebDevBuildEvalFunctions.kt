package com.arbr.object_model.functions.internal.code_eval

import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.Mono

interface WebDevBuildEvalFunctions {

    fun evaluateNpmBuild(
        projectName: String,
        volumeState: VolumeState,
    ): Mono<BuildFeedback>

}
