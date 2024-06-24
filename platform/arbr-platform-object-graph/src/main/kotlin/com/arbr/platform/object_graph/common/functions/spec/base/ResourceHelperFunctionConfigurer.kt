package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceHelperFunctionConfigurer<RV : ResourceView<*>> {
    val resourceViewClass: Class<RV>
}

