package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.functions.spec.base.BaseRFunctionSpecifier

class DefaultRFunctionSpecifier<RV: ResourceView<*>>(
    resourceViewClass: Class<RV>,
): BaseRFunctionSpecifier<RV>(
    resourceViewClass,
)
