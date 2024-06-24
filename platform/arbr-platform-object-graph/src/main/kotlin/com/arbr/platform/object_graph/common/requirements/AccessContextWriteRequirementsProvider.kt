package com.arbr.og.object_model.common.requirements

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial

/**
 * Write requirements provider in an access context.
 * Concretely: logs write dependencies for acquisition during trace passes.
 */
interface AccessContextWriteRequirementsProvider {
    /**
     * Add a write dependency.
     *
     * TODO: Should uValue be nullable?
     */
    fun <U : Any> requireWriteTo(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        uValue: U,
    ): U

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireWriteToChildren(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        resourceClass: Class<T>,
        mapValue: ImmutableLinkedMap<String, ObjectRef<out T, P, ForeignKey>>,
    ): ImmutableLinkedMap<String, T>

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireWriteToAttached(
        refStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        refClass: Class<ObjectRef<out T, P, ForeignKey>>,
        refValue: ObjectRef<out T, P, ForeignKey>,
    ): T
}