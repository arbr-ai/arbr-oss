package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.og.object_model.common.functions.api.ResourceFunction
import com.arbr.og.object_model.common.functions.spec.base.ResourceViewInstantiatorsFactory

data class ResourceFunctionConfig(
    val functions: List<ResourceFunction>,

    /**
     * Configured domain providers
     */
    val resourceViewProviderFactory: ResourceViewProviderFactory,
    val resourceStreamProviderFactory: ResourceStreamProviderFactory,
    val resourceViewInstantiatorsFactory: ResourceViewInstantiatorsFactory,
)
