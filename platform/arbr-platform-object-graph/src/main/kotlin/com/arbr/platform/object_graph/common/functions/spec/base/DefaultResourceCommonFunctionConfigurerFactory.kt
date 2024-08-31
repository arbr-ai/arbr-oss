package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext

class DefaultResourceCommonFunctionConfigurerFactory: ResourceCommonFunctionConfigurerFactory {

    override fun <RV : ResourceView<*>> makeResourceCommonFunctionConfigurer(
        context: ResourceFunctionContext,
        resourceViewClass: Class<RV>,
        contextName: String
    ): ResourceCommonFunctionConfigurer<RV> {
        return ResourceCommonFunctionConfigurerImpl(context, resourceViewClass, contextName)
    }
}