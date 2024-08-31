package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionConfigurableSet
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionConfiguredDependencies
import reactor.core.publisher.Mono

interface RFunctionDependencyTracer {
    fun <RV : ResourceView<*>, RK: NamedResourceKey> trace(
        configurableResourceFunctionSet: RFunctionConfigurableSet<RV, RK>,
    ): Mono<MutableList<RFunctionConfiguredDependencies<RV>>>
}

