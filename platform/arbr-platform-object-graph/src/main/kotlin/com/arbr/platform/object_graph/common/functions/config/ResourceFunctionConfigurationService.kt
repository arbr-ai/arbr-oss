package com.arbr.og.object_model.common.functions.config

import com.arbr.object_model.core.types.ResourceStreamProviderFactory
import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.og.object_model.common.functions.api.ResourceFunction
import com.arbr.og.object_model.common.functions.spec.base.ResourceViewInstantiatorsFactory
import com.arbr.og.object_model.common.functions.spec.impl.ResourceFunctionConfig

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
