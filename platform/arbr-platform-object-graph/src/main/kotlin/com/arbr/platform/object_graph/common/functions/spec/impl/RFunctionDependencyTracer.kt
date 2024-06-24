package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.functions.spec.base.RFunctionConfigurableSet
import com.arbr.og.object_model.common.functions.spec.base.RFunctionConfiguredDependencies
import reactor.core.publisher.Mono

interface RFunctionDependencyTracer {
    fun <RV : ResourceView<*>, RK: NamedResourceKey> trace(
        configurableResourceFunctionSet: RFunctionConfigurableSet<RV, RK>,
    ): Mono<MutableList<RFunctionConfiguredDependencies<RV>>>
}

