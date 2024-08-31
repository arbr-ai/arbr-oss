package com.arbr.platform.object_graph.types

interface ResourceStreamProviderFactory {

    fun <
            R : GeneralResource,
            RS : ResourceStream<R>,
            > resourceStreamProvider(
        resource: R,
    ): TypedResourceStreamProvider<R, RS>
}
