package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.functions.spec.base.BaseRFunctionFactory
import com.arbr.og.object_model.common.functions.spec.base.BaseRFunctionSpecifier

class DefaultRFunctionFactory : BaseRFunctionFactory() {
    override fun <RV : ResourceView<*>> newFunctionSpec(
        resourceViewClass: Class<RV>,
    ): BaseRFunctionSpecifier<RV> {
        return DefaultRFunctionSpecifier(resourceViewClass)
    }
}
