package com.arbr.og.object_model.common.functions.platform

import com.arbr.object_model.core.types.GeneralResource
import com.arbr.object_model.core.types.ResourceView

fun interface ResourceViewInstantiator<RV : ResourceView<*>> {

    fun newResource(): RV

}