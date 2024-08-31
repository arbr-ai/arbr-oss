package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.functions.platform.ResourceViewInstantiators
import com.arbr.platform.object_graph.common.functions.spec.impl.DependencyTracingProviderFactory
import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamTraceViewProvider

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
