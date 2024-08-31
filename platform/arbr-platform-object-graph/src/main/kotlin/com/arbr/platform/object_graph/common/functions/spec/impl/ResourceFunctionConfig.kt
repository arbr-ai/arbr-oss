package com.arbr.platform.object_graph.common.functions.spec.impl

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.common.functions.api.ResourceFunction
import com.arbr.platform.object_graph.common.functions.spec.base.ResourceViewInstantiatorsFactory

data class ResourceFunctionConfig(
    val functions: List<ResourceFunction>,

    /**
     * Configured domain providers
     */
    val resourceViewProviderFactory: ResourceViewProviderFactory,
    val resourceStreamProviderFactory: ResourceStreamProviderFactory,
    val resourceViewInstantiatorsFactory: ResourceViewInstantiatorsFactory,
)
