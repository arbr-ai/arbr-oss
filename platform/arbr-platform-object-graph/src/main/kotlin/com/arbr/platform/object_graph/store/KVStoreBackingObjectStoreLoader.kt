package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.impl.ObjectModelResource

interface KVStoreBackingObjectStoreLoader {

    fun <ForeignKey: NamedForeignKey> load(): KVStoreBackingObjectStore<ForeignKey, ObjectModelResource<*, *, ForeignKey>>
}
