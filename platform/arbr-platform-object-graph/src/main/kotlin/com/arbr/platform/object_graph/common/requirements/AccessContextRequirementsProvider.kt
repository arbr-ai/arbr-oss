package com.arbr.platform.object_graph.common.requirements

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.object_graph.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.model.ProposedForeignKeyCollectionStream
import com.arbr.platform.object_graph.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial

interface AccessContextRequirementsProvider {
    fun <U : Any> require(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
    ): U

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireChildren(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
    ): ImmutableLinkedMap<String, T>

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireAttached(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        tClass: Class<ObjectRef<out T, P, ForeignKey>>,
    ): T
}
