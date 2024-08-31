package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView

class RFunctionSetImpl<RV: ResourceView<*>>(
    override val functionSetName: String,
    override val mutators: List<RValueFunction<RV, Unit>>
) : RFunctionSet<RV>