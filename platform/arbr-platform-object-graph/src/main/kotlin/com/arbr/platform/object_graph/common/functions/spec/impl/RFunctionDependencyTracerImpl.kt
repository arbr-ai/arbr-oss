package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionConfigurableSet
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionConfiguredDependencies
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionTracingSessionProvider
import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionTracingSessionProviderImpl
import com.arbr.platform.object_graph.common.functions.spec.base.ResourceViewInstantiatorsFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Trace the dependencies of RFunctions.
 */
class RFunctionDependencyTracerImpl(
    resourceViewProviderFactory: ResourceViewProviderFactory,
    resourceStreamProviderFactory: ResourceStreamProviderFactory,
    dependencyTracingProviderFactory: DependencyTracingProviderFactory,
    resourceViewInstantiatorsFactory: ResourceViewInstantiatorsFactory,
) : RFunctionDependencyTracer {
    private val rFunctionTracingSessionProvider: RFunctionTracingSessionProvider = RFunctionTracingSessionProviderImpl(
        resourceViewProviderFactory,
        resourceStreamProviderFactory,
        dependencyTracingProviderFactory,
        resourceViewInstantiatorsFactory,
    )

    override fun <RV : ResourceView<*>, RK: NamedResourceKey> trace(
        configurableResourceFunctionSet: RFunctionConfigurableSet<RV, RK>,
    ): Mono<MutableList<RFunctionConfiguredDependencies<RV>>> {
        val dependencies = Flux.fromIterable(configurableResourceFunctionSet.mutators).concatMap { func ->
            val session = rFunctionTracingSessionProvider.createSession<RV, RK>(func)
            session.trace()
        }.collectList()

        return dependencies
    }
}