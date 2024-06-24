package com.arbr.core_web_dev.functions.example

import com.arbr.object_model.core.types.*
import com.arbr.object_model.functions.core.ExampleCommitCompletionFunction
import com.arbr.og.object_model.common.functions.api.ResourceFunction
import com.arbr.og.object_model.common.functions.config.ResourceFunctionConfigurationService
import com.arbr.og.object_model.common.functions.platform.ResourceViewDomainInstantiators
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiators
import com.arbr.og.object_model.common.functions.spec.base.ResourceViewDomainInstantiatorsFactory
import com.arbr.og.object_model.common.functions.spec.base.ResourceViewInstantiatorsFactory
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider

class ExampleFunctionConfigurationService : ResourceFunctionConfigurationService {
    override fun getResourceFunctions(): List<ResourceFunction> {
        return listOf(ExampleCommitCompletionFunction())
    }

    override fun getResourceViewProviderFactory(): ResourceViewProviderFactory {
        return ResourceViewProviders()
    }

    override fun getResourceStreamProviderFactory(): ResourceStreamProviderFactory {
        return ResourceStreamProviders()
    }

    override fun getResourceViewInstantiatorsFactory(): ResourceViewInstantiatorsFactory {
        TODO("Not yet implemented")
    }
}

class ArbrResourceViewDomainInstantiatorsFactory : ResourceViewDomainInstantiatorsFactory<ArbrResourceKey> {

    override fun makeInstantiators(
        resourceViewProviderFactory: ResourceViewProviderFactory,
        resourceStreamProviderFactory: ResourceStreamProviderFactory,
        proposedValueStreamViewProvider: ProposedValueStreamViewProvider<ArbrResourceKey>
    ): ResourceViewDomainInstantiators<ArbrResourceKey> {
        return ArbrResourceViewInstantiators(
            resourceViewProviderFactory,
            resourceStreamProviderFactory,
            proposedValueStreamViewProvider,
        )
    }

}
