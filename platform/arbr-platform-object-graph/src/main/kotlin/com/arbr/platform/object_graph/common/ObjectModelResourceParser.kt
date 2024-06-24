package com.arbr.platform.object_graph.common

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.alignable.PartialNodeAlignableValue
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial

interface ObjectModelResourceParser<
        T : ObjectModelResource<T, P, ForeignKey>,
        P : Partial<T, P, ForeignKey>,
        ForeignKey: NamedForeignKey,
        > {

    /**
     * Key for matching the resource type.
     */
    val key: String

    /**
     * Parse the resource type from a node value.
     */
    fun parseNodeValue(
        nodeValue: PartialNodeAlignableValue,
    ): ObjectModelResource<T, P, ForeignKey>

}
