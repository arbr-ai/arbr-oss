package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.types.ResourceStream
import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamViewProvider

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