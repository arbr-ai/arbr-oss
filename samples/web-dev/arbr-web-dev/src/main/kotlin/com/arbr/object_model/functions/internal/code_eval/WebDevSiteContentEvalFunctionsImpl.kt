package com.arbr.object_model.functions.internal.code_eval

import com.arbr.platform.object_graph.file_system.VolumeState
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class WebDevSiteContentEvalFunctionsImpl: WebDevSiteContentEvalFunctions {
    override fun evaluatePageLoad(
        projectName: String,
        volumeState: VolumeState,
        pageHref: String
    ): Mono<BuildFeedback> {
        TODO("Not yet implemented")
    }
}
