package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey

interface PartialNode<P : Partial<*, *, ForeignKey>, ForeignKey: NamedForeignKey> {
    val uuid: String
    val resourceTypeName: String
    val properties: PartialProperties<P>
    val parents: PartialParents<P, ForeignKey>
    val children: PartialChildren<P, ForeignKey>
}