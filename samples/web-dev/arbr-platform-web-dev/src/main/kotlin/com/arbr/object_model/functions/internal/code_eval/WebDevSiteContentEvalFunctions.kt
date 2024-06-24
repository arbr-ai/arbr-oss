package com.arbr.object_model.functions.internal.code_eval

import com.arbr.og_engine.file_system.VolumeState
import reactor.core.publisher.Mono

interface WebDevSiteContentEvalFunctions {

    /**
     * Evaluate a page load attempt
     */
    fun evaluatePageLoad(
        projectName: String,
        volumeState: VolumeState,
        pageHref: String,

        /**
         * TODO: Needs second step translating result -> eval
         */
        // fetchResult: HeadlessDocumentResult?
    ): Mono<BuildFeedback>
}