package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.impl.ObjectModelResource
import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapBackingStoreLoader : KVStoreBackingObjectStoreLoader {

    override fun <ForeignKey : NamedForeignKey> load(): KVStoreBackingObjectStore<
            ForeignKey,
            ObjectModelResource<*, *, ForeignKey>
            > {
        return ConcurrentHashMapBackingStore(
            ConcurrentHashMap(),
            ConcurrentHashMap(),
            ConcurrentHashMap(),
            ConcurrentHashMap(),
        )
    }
}