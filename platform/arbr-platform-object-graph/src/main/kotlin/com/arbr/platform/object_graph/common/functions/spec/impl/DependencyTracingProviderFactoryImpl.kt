package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.common.properties.DefaultMapReadWriteDependencyTracingProvider
import com.arbr.platform.object_graph.common.properties.DependencyTracingProvider

class DependencyTracingProviderFactoryImpl(
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceStreamProviderFactory: ResourceStreamProviderFactory,
) : DependencyTracingProviderFactory {
    override fun newTracingProvider(): DependencyTracingProvider {
        return DefaultMapReadWriteDependencyTracingProvider(
            resourceViewProviderFactory,
            resourceStreamProviderFactory
        )
    }
}