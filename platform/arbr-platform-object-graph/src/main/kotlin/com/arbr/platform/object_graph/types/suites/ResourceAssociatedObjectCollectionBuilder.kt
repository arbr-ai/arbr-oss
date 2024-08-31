package com.arbr.platform.object_graph.types.suites

interface ResourceAssociatedObjectCollectionBuilder<E : EnumLike, T: Any> {

    fun buildWith(
        transform: (E) -> T
    ): ResourceAssociatedObjectCollection<E, T>
}
