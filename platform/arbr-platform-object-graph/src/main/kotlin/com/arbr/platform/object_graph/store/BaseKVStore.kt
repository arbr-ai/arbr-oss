package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ForeignKeyChildListResult
import com.arbr.platform.object_graph.common.ForeignKeyChildSingleParentUpdate
import reactor.core.publisher.Flux

abstract class BaseKVStore<ForeignKey: NamedForeignKey, Value: Any>(
    private val backingStore: KVStoreBackingObjectStore<ForeignKey, Value>
): KVStore<String, Value, ForeignKey> {

    abstract fun handleNewElement(newElement: Value)

    abstract fun childResourceParentUpdates(foreignKey: ForeignKey, parentUuid: String): Flux<ForeignKeyChildSingleParentUpdate<ForeignKey, Value>>

    override operator fun get(key: String): Value? {
        return backingStore.getObject(key)
    }

    override fun computeIfAbsent(key: String, mappingFunction: (String) -> Value): Value {
        return backingStore.computeIfAbsent(key, mappingFunction) { newElement ->
            handleNewElement(newElement)
        }
    }

    override fun foreignKeyListIndex(
        foreignKey: ForeignKey,
        parentKey: String
    ): Flux<out ForeignKeyChildListResult<String, Value>> {
        return childResourceParentUpdates(foreignKey, parentKey)
            .map { update ->
                backingStore.computeUpdatedForeignKeyChildren(update)
            }
    }

    override fun queryForeignKey(foreignKey: ForeignKey, parentKey: String): Collection<Value> {
        return backingStore.queryForeignKey(foreignKey, parentKey)
    }
}