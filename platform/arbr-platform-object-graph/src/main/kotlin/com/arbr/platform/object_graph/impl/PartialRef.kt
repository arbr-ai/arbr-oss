package com.arbr.platform.object_graph.impl

data class PartialRef<T: ObjectModelResource<T, P, *>, P: Partial<T, P, *>>(
    val uuid: String?,
)
