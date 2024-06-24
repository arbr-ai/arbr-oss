package com.arbr.og.object_model.common.properties

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.og.object_model.common.model.PropertyIdentifier
import java.util.concurrent.ConcurrentHashMap

class DefaultMapReadWriteDependencyTracingProvider private constructor(
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceStreamProviderFactory: ResourceStreamProviderFactory,
    private val valueMap: ConcurrentHashMap<PropertyIdentifier, Any>,
) : ReadDependencyTracingProvider by DefaultMapReadDependencyTracingProvider(
    resourceViewProviderFactory,
    resourceStreamProviderFactory,
    valueMap,
), WriteDependencyTracingProvider by MapWriteDependencyTracingProvider(valueMap), DependencyTracingProvider {
    constructor(
        resourceViewProviderFactory: ResourceViewProviderFactory,
        resourceStreamProviderFactory: ResourceStreamProviderFactory,
    ) : this(resourceViewProviderFactory, resourceStreamProviderFactory, ConcurrentHashMap())
}