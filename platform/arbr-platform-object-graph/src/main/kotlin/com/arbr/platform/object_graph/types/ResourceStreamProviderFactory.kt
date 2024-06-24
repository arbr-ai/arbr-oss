package com.arbr.object_model.core.types

interface ResourceStreamProviderFactory {

    fun <
            R : GeneralResource,
            RS : ResourceStream<R>,
            > resourceStreamProvider(
        resource: R,
    ): TypedResourceStreamProvider<R, RS>
}
