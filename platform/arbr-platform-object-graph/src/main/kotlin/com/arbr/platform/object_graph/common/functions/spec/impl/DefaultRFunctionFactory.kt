package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.functions.spec.base.BaseRFunctionFactory
import com.arbr.platform.object_graph.common.functions.spec.base.BaseRFunctionSpecifier

class DefaultRFunctionFactory : BaseRFunctionFactory() {
    override fun <RV : ResourceView<*>> newFunctionSpec(
        resourceViewClass: Class<RV>,
    ): BaseRFunctionSpecifier<RV> {
        return DefaultRFunctionSpecifier(resourceViewClass)
    }
}
