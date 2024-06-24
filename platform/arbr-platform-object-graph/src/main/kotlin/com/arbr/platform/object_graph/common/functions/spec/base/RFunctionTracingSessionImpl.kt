package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.og.object_model.common.properties.DependencyTracingProvider
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContext
import com.arbr.og.object_model.common.functions.platform.ResourceFunctionContextImpl
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiators
import reactor.core.publisher.Mono

class RFunctionTracingSessionImpl<RV : ResourceView<*>>(
    private val resourceViewInstantiators: ResourceViewInstantiators,
    private val dependencyTracingProvider: DependencyTracingProvider,
    private val configurableFunction: RFunctionConfigurable<RV>
) : RFunctionTracingSession<RV> {
    private fun makeConfigurators(): ResourceFunctionContext {
        return ResourceFunctionContextImpl(
            resourceViewInstantiators,
            dependencyTracingProvider,
        )
    }

    override fun trace(): Mono<RFunctionConfiguredDependencies<RV>> {
        val context = makeConfigurators()
        val resourceFunction = configurableFunction.configure(context)

        // ...
        val resourceView = context.new(resourceFunction.resourceViewClass)
        return resourceFunction.apply(resourceView)
            .materialize()
            .map {
                // TODO: Push a new session onto the provider
                val dependencySuite = dependencyTracingProvider.collectDependencies()

                RFunctionConfiguredDependencies(
                    resourceFunction,
                    dependencySuite,
                )
            }
    }
}