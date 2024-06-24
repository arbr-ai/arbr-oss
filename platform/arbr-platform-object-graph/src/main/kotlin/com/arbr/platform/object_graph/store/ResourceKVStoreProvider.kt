package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.impl.ObjectModelResource

interface ResourceKVStoreProvider<ForeignKey: NamedForeignKey> {

    fun provide(): KVStore<String, ObjectModelResource<*, *, ForeignKey>, ForeignKey>
}
