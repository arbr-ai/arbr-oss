package com.arbr.object_model.core.types

fun interface TypedResourceViewProvider<
        R : GeneralResource,
        RV : ResourceView<R>,
        > {
    fun provideResourceView(
        resourceStream: ResourceStream<R>,
    ): RV
}

