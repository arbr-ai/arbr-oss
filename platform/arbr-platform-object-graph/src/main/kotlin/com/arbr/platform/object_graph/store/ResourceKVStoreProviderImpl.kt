package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.impl.ObjectModelResource

class ResourceKVStoreProviderImpl<ForeignKey: NamedForeignKey>(
    private val kvStore: KVStore<String, ObjectModelResource<*, *, ForeignKey>, ForeignKey>,
): ResourceKVStoreProvider<ForeignKey> {
    override fun provide(): KVStore<String, ObjectModelResource<*, *, ForeignKey>, ForeignKey> {
        return kvStore
    }
}