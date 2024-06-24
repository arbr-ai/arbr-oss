package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiators
import com.arbr.og.object_model.common.functions.spec.impl.DependencyTracingProviderFactory
import com.arbr.og.object_model.common.model.view.ProposedValueStreamTraceViewProvider

class RFunctionTracingSessionProviderImpl(
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceStreamProviderFactory: ResourceStreamProviderFactory,
    private val dependencyTracingProviderFactory: DependencyTracingProviderFactory,
    private val resourceViewInstantiatorsFactory: ResourceViewInstantiatorsFactory,
): RFunctionTracingSessionProvider {
    override fun <RV : ResourceView<*>, RK: NamedResourceKey> createSession(
        configurableFunction: RFunctionConfigurable<RV>
    ): RFunctionTracingSession<RV> {
        val newTracingProvider = dependencyTracingProviderFactory.newTracingProvider()

        // TODO: Make configurable
        val proposedValueStreamViewProvider = ProposedValueStreamTraceViewProvider<RK>(
            newTracingProvider,
            newTracingProvider,
        )
        val resourceViewInstantiators: ResourceViewInstantiators = resourceViewInstantiatorsFactory.makeInstantiators(
            resourceViewProviderFactory, resourceStreamProviderFactory, proposedValueStreamViewProvider
        )

        return RFunctionTracingSessionImpl(
            resourceViewInstantiators,
            newTracingProvider,
            configurableFunction,
        )
    }
}
