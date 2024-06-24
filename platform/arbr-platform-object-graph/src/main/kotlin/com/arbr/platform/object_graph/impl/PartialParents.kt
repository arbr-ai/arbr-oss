package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey

interface PartialParents<P : Partial<*, *, *>, ForeignKey: NamedForeignKey> {
    val parentMap: Map<ForeignKey, PartialRef<*, *>>
}