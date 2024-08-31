package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

abstract class BaseRFunctionFactory : RFunctionFactory() {
    private val providers = mutableListOf<RFunctionConfigurableSetDelegateProvider<*, *>>()
    private val specs = mutableListOf<BaseRFunctionSpecifier<*>>()

    abstract fun <RV : ResourceView<*>> newFunctionSpec(
        resourceViewClass: Class<RV>,
    ): BaseRFunctionSpecifier<RV>

    /**
     * Defer specific configurators as long as possible to enable swizzling tracers
     */
    override fun <RV : ResourceView<R>, R: NamedResource<*, RK, *, *>, RK: NamedResourceKey> actingOnResource(
        resourceViewClass: Class<RV>,
        configure: RFunctionSpecifier<RV>.() -> Unit
    ): RFunctionConfigurableSetDelegateProvider<RV, RK> {
        val provider = RFunctionConfigurableSetDelegateProvider { functionSetName ->
            val newFunctionSpec = newFunctionSpec<RV>(resourceViewClass)
            configure(newFunctionSpec)
            specs.add(newFunctionSpec)

            // Hack until I can figure out how delegates are supposed to work
            newFunctionSpec.collectConfigurableFunctions()

            newFunctionSpec.buildConfigurableFunctionSet<RK>(functionSetName)
        }
        providers.add(provider)
        return provider
    }

    fun collectConfigurableFunctionSets(): List<RFunctionConfigurableSet<out ResourceView<*>, out NamedResourceKey>> {
        val sets = providers.map {
            it.getOrConfigure()
        }
        specs.forEach { it.collectConfigurableFunctions() }
        return sets
    }
}

