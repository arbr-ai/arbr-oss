package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.functions.platform.ResourceViewDomainInstantiators
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiators
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider

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
