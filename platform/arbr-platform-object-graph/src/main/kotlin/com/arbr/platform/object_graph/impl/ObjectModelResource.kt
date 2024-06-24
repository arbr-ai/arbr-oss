package com.arbr.platform.object_graph.impl

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueStream
import com.fasterxml.jackson.annotation.JsonIgnore

interface ObjectModelResource<
        T : ObjectModelResource<T, P, ForeignKey>,
        P : Partial<T, P, ForeignKey>,
        ForeignKey : NamedForeignKey,
        > : MutableParentKeyedObject<ForeignKey> {

    val uuid: String

    val resourceTypeName: String

    @JsonIgnore
    fun properties(): ImmutableLinkedMap<String, ProposedValueStream<ObjectModel.ObjectValue<*, *, *, *>>>

    @JsonIgnore
    fun getChildren(): Map<ForeignKey, ProposedForeignKeyCollectionStream<*, *, ForeignKey>>

    @JsonIgnore
    fun getForeignKeys(): Map<ForeignKey, ProposedValueStream<ObjectRef<*, *, ForeignKey>>>

    @JsonIgnore
    fun toPartial(
        partialObjectGraph: PartialObjectGraph<*, *, ForeignKey>,
    ): P

    fun toPartialErased(
        partialObjectGraph: PartialObjectGraph<*, *, ForeignKey>,
    ): Partial<*, *, ForeignKey> = toPartial(partialObjectGraph) as Partial<*, *, ForeignKey>

}
