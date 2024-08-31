package com.arbr.platform.object_graph.impl

import com.arbr.platform.object_graph.types.naming.NamedForeignKey

interface PartialParents<P : Partial<*, *, *>, ForeignKey: NamedForeignKey> {
    val parentMap: Map<ForeignKey, PartialRef<*, *>>
}