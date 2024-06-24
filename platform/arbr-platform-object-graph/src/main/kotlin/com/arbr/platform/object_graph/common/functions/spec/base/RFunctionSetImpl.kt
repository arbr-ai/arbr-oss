package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

class RFunctionSetImpl<RV: ResourceView<*>>(
    override val functionSetName: String,
    override val mutators: List<RValueFunction<RV, Unit>>
) : RFunctionSet<RV>