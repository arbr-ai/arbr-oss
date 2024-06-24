package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedSet

interface PartialChildren<P : Partial<*, *, *>, ForeignKey: NamedForeignKey> {
    val foreignKeyChildMap: Map<ForeignKey, ImmutableLinkedSet<String>>
}