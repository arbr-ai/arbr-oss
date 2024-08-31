package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext

fun interface RValueFunctionConfigurable<RV : ResourceView<*>, T>: ResourceConfigurableTarget<RValueFunction<RV, T>> {

    override fun configure(context: ResourceFunctionContext): RValueFunction<RV, T>

}

fun interface RFunctionConfigurable<RV : ResourceView<*>>: RValueFunctionConfigurable<RV, Unit>
