package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

interface ResourceCommonFunctionConfigurer<RV : ResourceView<*>>: ResourceHelperFunctionConfigurer<RV>,
    ResourceCompletionHelperFunctionConfigurer<RV>,
    ResourceEmbeddingHelperFunctionConfigurer<RV>