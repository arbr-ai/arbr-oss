package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceCommonFunctionConfigurer<RV : ResourceView<*>>: ResourceHelperFunctionConfigurer<RV>,
    ResourceCompletionHelperFunctionConfigurer<RV>,
    ResourceEmbeddingHelperFunctionConfigurer<RV>