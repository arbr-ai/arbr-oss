package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceCompletionHelperFunctionConfigurer<RV : ResourceView<*>> : ResourceHelperFunctionConfigurer<RV>,
    ResourceHelperFunctionInputSpecConfigurer<RV>, ResourceHelperFunctionReturnSpecConfigurer<RV> {
    fun complete(configure: ResourceHelperFunctionCompleteSpecContext.() -> Unit)
}
