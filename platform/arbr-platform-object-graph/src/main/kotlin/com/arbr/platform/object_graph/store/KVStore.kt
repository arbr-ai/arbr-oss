package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ForeignKeyChildListResult
import reactor.core.publisher.Flux

interface KVStore<Key : Any, Value : Any, ForeignKey : NamedForeignKey> {
    operator fun get(key: Key): Value?

    fun computeIfAbsent(key: Key, mappingFunction: (Key) -> Value): Value

    fun foreignKeyListIndex(foreignKey: ForeignKey, parentKey: Key): Flux<out ForeignKeyChildListResult<Key, Value>>

    fun queryForeignKey(foreignKey: ForeignKey, parentKey: Key): Collection<Value>
}
