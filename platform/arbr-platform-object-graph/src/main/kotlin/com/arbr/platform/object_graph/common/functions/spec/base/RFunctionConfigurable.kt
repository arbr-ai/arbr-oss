package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext

fun interface RValueFunctionConfigurable<RV : ResourceView<*>, T>: ResourceConfigurableTarget<RValueFunction<RV, T>> {

    override fun configure(context: ResourceFunctionContext): RValueFunction<RV, T>

}

fun interface RFunctionConfigurable<RV : ResourceView<*>>: RValueFunctionConfigurable<RV, Unit>
