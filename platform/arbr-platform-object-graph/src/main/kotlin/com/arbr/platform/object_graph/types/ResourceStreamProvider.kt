package com.arbr.object_model.core.types

interface ResourceStreamProvider<
    R: GeneralResource
>: TypedResourceStreamProvider<R, ResourceStream<R>>
