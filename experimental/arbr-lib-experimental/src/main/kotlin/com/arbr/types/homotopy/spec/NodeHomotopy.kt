package com.arbr.types.homotopy.spec

import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.htype.NodeHType
import com.arbr.types.homotopy.keys.CompoundKeyPathPropertyKey

interface NodeHomotopy<Tr> {
    /**
     * To break cycles without requiring acyclic graphs, node lifting may not use refs
     */
    fun liftNode(
        context: MutablePathContext,
        valueTypeImplementor: Tr,
        innerImplementors: List<Tr>
    ): Tr

    fun <F: Tr> constructNode(
        value: F,
        childHTypes: List<Pair<String, NodeHType<Tr, F>>>,
        context: MutablePathContext,
    ): NodeHType<Tr, F> {
        val imp = liftNode(
            context,
            value,
            childHTypes.map { it.second.implementor },
        )
        return NodeHType(
            CompoundKeyPathPropertyKey(context.pathTokens.toList()),
            value,
            childHTypes,
            imp,
        )
    }
}

