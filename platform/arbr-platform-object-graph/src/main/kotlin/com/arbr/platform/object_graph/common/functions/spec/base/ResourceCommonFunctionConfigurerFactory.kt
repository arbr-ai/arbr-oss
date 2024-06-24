package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext

interface ResourceCommonFunctionConfigurerFactory {

    fun <RV : ResourceView<*>> makeResourceCommonFunctionConfigurer(
        context: ResourceFunctionContext,
        resourceViewClass: Class<RV>,
        contextName: String,
    ): ResourceCommonFunctionConfigurer<RV>
}