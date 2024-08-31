package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

interface ResourceCompletionHelperFunctionConfigurer<RV : ResourceView<*>> : ResourceHelperFunctionConfigurer<RV>,
    ResourceHelperFunctionInputSpecConfigurer<RV>, ResourceHelperFunctionReturnSpecConfigurer<RV> {
    fun complete(configure: ResourceHelperFunctionCompleteSpecContext.() -> Unit)
}
