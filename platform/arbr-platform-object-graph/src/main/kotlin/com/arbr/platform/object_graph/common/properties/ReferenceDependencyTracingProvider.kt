package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStream
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider

interface ReferenceDependencyTracingProvider {
    fun <
            RK : NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>,
            RS : ResourceStream<R>,
            > getOuterReferenceValue(
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
        containerIdentifier: PropertyIdentifier,
    ): RV
}