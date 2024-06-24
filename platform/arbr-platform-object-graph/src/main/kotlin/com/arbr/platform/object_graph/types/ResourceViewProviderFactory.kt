package com.arbr.object_model.core.types

import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider

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