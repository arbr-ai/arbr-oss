package com.arbr.object_model.functions.internal.code_eval

import com.arbr.platform.object_graph.file_system.VolumeState
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WebDevBuildEvalFunctionsImpl: WebDevBuildEvalFunctions {
    override fun evaluateNpmBuild(projectName: String, volumeState: VolumeState): Mono<BuildFeedback> {
        TODO("Not yet implemented")
    }

}
