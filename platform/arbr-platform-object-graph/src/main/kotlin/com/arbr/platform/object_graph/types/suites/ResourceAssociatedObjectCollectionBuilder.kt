package com.arbr.object_model.core.types.suites

interface ResourceAssociatedObjectCollectionBuilder<E : EnumLike, T: Any> {

    fun buildWith(
        transform: (E) -> T
    ): ResourceAssociatedObjectCollection<E, T>
}
