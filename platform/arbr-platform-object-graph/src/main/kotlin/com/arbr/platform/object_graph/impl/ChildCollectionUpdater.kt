package com.arbr.platform.object_graph.impl

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

interface ChildCollectionUpdater<
        T : ObjectModelResource<T, P, *>,
        P : Partial<T, P, *>,
        > {

    fun update(collection: ImmutableLinkedMap<String, P>?): ImmutableLinkedMap<String, P>?

    companion object {
        fun <T : ObjectModelResource<T, P, *>, P : Partial<T, P, *>> of(
            updateFunc: (ImmutableLinkedMap<String, P>?) -> ImmutableLinkedMap<String, P>?
        ) = object : ChildCollectionUpdater<T, P> {
            override fun update(collection: ImmutableLinkedMap<String, P>?): ImmutableLinkedMap<String, P>? {
                return updateFunc(collection)
            }
        }
    }

}