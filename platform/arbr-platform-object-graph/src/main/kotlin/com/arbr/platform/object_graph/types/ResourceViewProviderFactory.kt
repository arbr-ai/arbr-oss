package com.arbr.platform.object_graph.types

import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamViewProvider

interface ResourceViewProviderFactory {
    fun <
            RK: NamedResourceKey,
            R : NamedResource<*, RK, *, *>,
            RV : ResourceView<R>,
            > resourceViewProvider(
        proposedValueStreamProvider: ProposedValueStreamViewProvider<RK>,
        resource: R,
    ): TypedResourceViewProvider<R, RV>
}