package com.arbr.platform.object_graph.types

fun interface TypedResourceStreamProvider<
        R : GeneralResource,
        RS : ResourceStream<R>,
        > {
    fun provideEmptyResource(uuid: String): ResourceStream<R>
}
