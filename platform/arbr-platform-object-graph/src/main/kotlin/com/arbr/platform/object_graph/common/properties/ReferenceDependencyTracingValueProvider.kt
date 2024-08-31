package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.model.PropertyIdentifier

fun interface ReferenceDependencyTracingValueProvider<
        RK : NamedResourceKey,
        R : NamedResource<*, RK, *, *>,
        RV : ResourceView<R>
        > {
    fun getOuterReferenceValue(
        containerIdentifier: PropertyIdentifier
    ): RV
}