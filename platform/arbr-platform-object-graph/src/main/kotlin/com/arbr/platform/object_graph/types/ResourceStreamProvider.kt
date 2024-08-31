package com.arbr.platform.object_graph.types

interface ResourceStreamProvider<
    R: GeneralResource
>: TypedResourceStreamProvider<R, ResourceStream<R>>
