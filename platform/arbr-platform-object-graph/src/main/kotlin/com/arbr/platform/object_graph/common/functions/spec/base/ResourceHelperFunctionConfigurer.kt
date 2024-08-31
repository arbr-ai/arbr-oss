package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

interface ResourceHelperFunctionConfigurer<RV : ResourceView<*>> {
    val resourceViewClass: Class<RV>
}

