package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

interface RFunctionTracingSessionProvider {

    fun <RV : ResourceView<*>, RK: NamedResourceKey> createSession(
        configurableFunction: RFunctionConfigurable<RV>
    ): RFunctionTracingSession<RV>

}
