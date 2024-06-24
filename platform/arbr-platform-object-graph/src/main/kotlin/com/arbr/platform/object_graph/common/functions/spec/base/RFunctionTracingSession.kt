package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import reactor.core.publisher.Mono

interface RFunctionTracingSession<RV: ResourceView<*>> {
    fun trace(): Mono<RFunctionConfiguredDependencies<RV>>
}