package com.arbr.object_model.core.types

fun interface TypedResourceStreamProvider<
        R : GeneralResource,
        RS : ResourceStream<R>,
        > {
    fun provideEmptyResource(uuid: String): ResourceStream<R>
}
