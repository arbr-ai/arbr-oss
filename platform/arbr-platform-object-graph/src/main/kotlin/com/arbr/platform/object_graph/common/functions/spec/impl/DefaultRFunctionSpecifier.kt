package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.functions.spec.base.BaseRFunctionSpecifier

class DefaultRFunctionSpecifier<RV: ResourceView<*>>(
    resourceViewClass: Class<RV>,
): BaseRFunctionSpecifier<RV>(
    resourceViewClass,
)
