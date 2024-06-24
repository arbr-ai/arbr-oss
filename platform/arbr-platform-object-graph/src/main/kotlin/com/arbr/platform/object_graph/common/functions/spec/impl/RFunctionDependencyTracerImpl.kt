package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.og.object_model.common.functions.spec.base.*
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