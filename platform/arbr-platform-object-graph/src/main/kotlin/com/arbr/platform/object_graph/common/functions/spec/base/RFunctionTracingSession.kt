package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import reactor.core.publisher.Mono

interface RFunctionTracingSession<RV: ResourceView<*>> {
    fun trace(): Mono<RFunctionConfiguredDependencies<RV>>
}