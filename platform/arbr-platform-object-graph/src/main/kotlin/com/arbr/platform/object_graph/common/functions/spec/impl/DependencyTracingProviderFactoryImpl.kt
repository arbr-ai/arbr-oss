package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.og.object_model.common.properties.DefaultMapReadWriteDependencyTracingProvider
import com.arbr.og.object_model.common.properties.DependencyTracingProvider

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