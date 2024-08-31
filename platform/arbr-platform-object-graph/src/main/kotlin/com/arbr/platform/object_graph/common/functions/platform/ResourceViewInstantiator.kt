package com.arbr.platform.object_graph.common.functions.platform

import com.arbr.platform.object_graph.types.ResourceView

fun interface ResourceViewInstantiator<RV : ResourceView<*>> {

    fun newResource(): RV

}