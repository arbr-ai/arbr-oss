package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext

interface ResourceCommonFunctionConfigurerFactory {

    fun <RV : ResourceView<*>> makeResourceCommonFunctionConfigurer(
        context: ResourceFunctionContext,
        resourceViewClass: Class<RV>,
        contextName: String,
    ): ResourceCommonFunctionConfigurer<RV>
}