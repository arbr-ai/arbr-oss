package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResourceKey

interface RFunctionSet<RV: ResourceView<*>> {
    val functionSetName: String
    val mutators: List<RValueFunction<RV, Unit>>
}

class RFunctionConfigurableSet<RV: ResourceView<*>, RK: NamedResourceKey>(
    val functionSetName: String,
    val mutators: List<RFunctionConfigurable<RV>>,
)
