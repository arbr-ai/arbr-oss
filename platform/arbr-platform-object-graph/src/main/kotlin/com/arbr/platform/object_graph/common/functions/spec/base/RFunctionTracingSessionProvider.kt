package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResourceKey

interface RFunctionTracingSessionProvider {

    fun <RV : ResourceView<*>, RK: NamedResourceKey> createSession(
        configurableFunction: RFunctionConfigurable<RV>
    ): RFunctionTracingSession<RV>

}
