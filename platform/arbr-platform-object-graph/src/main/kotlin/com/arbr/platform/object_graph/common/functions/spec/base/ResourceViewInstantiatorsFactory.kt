package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.functions.platform.ResourceViewDomainInstantiators
import com.arbr.platform.object_graph.common.functions.platform.ResourceViewInstantiators
import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamViewProvider

interface ResourceViewInstantiatorsFactory {

    fun makeInstantiators(
        resourceViewProviderFactory: ResourceViewProviderFactory,
        resourceStreamProviderFactory: ResourceStreamProviderFactory,
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<*>,
    ): ResourceViewInstantiators
}

interface ResourceViewDomainInstantiatorsFactory<RK: NamedResourceKey> {

    fun makeInstantiators(
        resourceViewProviderFactory: ResourceViewProviderFactory,
        resourceStreamProviderFactory: ResourceStreamProviderFactory,
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<RK>,
    ): ResourceViewDomainInstantiators<RK>
}
