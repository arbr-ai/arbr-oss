package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

interface RFunctionSet<RV: ResourceView<*>> {
    val functionSetName: String
    val mutators: List<RValueFunction<RV, Unit>>
}

class RFunctionConfigurableSet<RV: ResourceView<*>, RK: NamedResourceKey>(
    val functionSetName: String,
    val mutators: List<RFunctionConfigurable<RV>>,
)
