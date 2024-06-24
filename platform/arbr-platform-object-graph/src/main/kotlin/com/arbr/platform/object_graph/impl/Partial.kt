package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey

interface Partial<T: ObjectModelResource<T, P, ForeignKey>, P: Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>:
    PartialNode<P, ForeignKey> {

    fun updateParentConditionally(
        key: ForeignKey,
        expectedUuid: String?,
        newUuid: String?,
    )

    fun <
            U : ObjectModelResource<U, Q, ForeignKey>,
            Q : Partial<U, Q, ForeignKey>,
            > updateChildren(
        key: ForeignKey,
        updater: ChildCollectionUpdater<U, Q>
    )
}
