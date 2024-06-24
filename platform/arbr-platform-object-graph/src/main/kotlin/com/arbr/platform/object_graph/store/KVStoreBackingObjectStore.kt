package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ForeignKeyChildListResultStringImpl
import com.arbr.platform.object_graph.common.ForeignKeyChildSingleParentUpdate

interface KVStoreBackingObjectStore<ForeignKey: NamedForeignKey, T: Any> {

    fun getObject(uuid: String): T?

    fun computeIfAbsent(
        key: String,
        mappingFunction: (String) -> T,
        handleNewElement: (T) -> Unit,
    ): T

    fun computeUpdatedForeignKeyChildren(
        childUpdate: ForeignKeyChildSingleParentUpdate<ForeignKey, T>
    ): ForeignKeyChildListResultStringImpl<T>

    fun queryForeignKey(foreignKey: ForeignKey, parentUuid: String): Collection<T>

    fun getCreationOrdinal(uuid: String): Int?

}