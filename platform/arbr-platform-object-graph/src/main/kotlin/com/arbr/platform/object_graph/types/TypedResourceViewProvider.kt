package com.arbr.platform.object_graph.types

fun interface TypedResourceViewProvider<
        R : GeneralResource,
        RV : ResourceView<R>,
        > {
    fun provideResourceView(
        resourceStream: ResourceStream<R>,
    ): RV
}

