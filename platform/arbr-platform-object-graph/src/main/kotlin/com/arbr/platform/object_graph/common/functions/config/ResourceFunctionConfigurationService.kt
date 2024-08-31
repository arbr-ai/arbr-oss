package com.arbr.platform.object_graph.common.functions.config

import com.arbr.platform.object_graph.types.ResourceStreamProviderFactory
import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.common.functions.api.ResourceFunction
import com.arbr.platform.object_graph.common.functions.spec.base.ResourceViewInstantiatorsFactory
import com.arbr.platform.object_graph.common.functions.spec.impl.ResourceFunctionConfig

interface ResourceFunctionConfigurationService {

    fun getResourceFunctions(): List<ResourceFunction>

    fun getResourceViewProviderFactory(): ResourceViewProviderFactory
    fun getResourceStreamProviderFactory(): ResourceStreamProviderFactory
    fun getResourceViewInstantiatorsFactory(): ResourceViewInstantiatorsFactory

    fun renderConfig(): ResourceFunctionConfig {
        return ResourceFunctionConfig(
            getResourceFunctions(),
            getResourceViewProviderFactory(),
            getResourceStreamProviderFactory(),
            getResourceViewInstantiatorsFactory(),
        )
    }
}
