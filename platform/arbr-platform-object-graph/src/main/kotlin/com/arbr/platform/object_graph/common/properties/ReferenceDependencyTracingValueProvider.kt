package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.model.PropertyIdentifier

fun interface ReferenceDependencyTracingValueProvider<
        RK : NamedResourceKey,
        R : NamedResource<*, RK, *, *>,
        RV : ResourceView<R>
        > {
    fun getOuterReferenceValue(
        containerIdentifier: PropertyIdentifier
    ): RV
}