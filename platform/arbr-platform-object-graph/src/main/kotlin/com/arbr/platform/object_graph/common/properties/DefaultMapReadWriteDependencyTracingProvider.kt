package com.arbr.platform.object_graph.common.properties

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.common.model.PropertyIdentifier
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