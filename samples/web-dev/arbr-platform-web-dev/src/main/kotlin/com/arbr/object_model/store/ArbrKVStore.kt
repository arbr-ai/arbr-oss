package com.arbr.object_model.store

import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.store.DefaultKVStore
import com.arbr.platform.object_graph.store.KVStore
import com.arbr.platform.object_graph.impl.ObjectModelResource

// TODO: Templatize
class ArbrKVStore private constructor(
    innerKVStore: KVStore<String, ObjectModelResource<*, *, ArbrForeignKey>, ArbrForeignKey>,
): KVStore<String, ObjectModelResource<*, *, ArbrForeignKey>, ArbrForeignKey> by innerKVStore {

    companion object {
        fun load(): ArbrKVStore {
            return ArbrKVStore(DefaultKVStore.loadConfiguredKvStore())
        }
    }
}

