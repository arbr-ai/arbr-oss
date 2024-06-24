package com.arbr.types.homotopy.htype

import com.arbr.types.homotopy.keys.CompoundKeyPathPropertyKey

/**
 * A node is a value property along with a list of child properties, empty for leaves
 * This type class is special because its homotopy is dictated solely by its own value,
 * i.e. only if its value admits a trait will the whole node.
 *
 * In other words it acts as a discrete gate in the homotopic propagation of traits
 *
 * We use a list rather than a nested struct to not require struct homotopy which is very liberal
 */
class NodeHType<Tr, F : Tr>(
    override val keyPath: CompoundKeyPathPropertyKey,
    val baseValue: F,
    val childHTypes: List<Pair<String, NodeHType<Tr, F>>>,
    override val implementor: Tr,
) : HType<Tr, F> {

    override fun toString(): String {
        return "Node[key=\"${keyPath}\", value=\"${baseValue}\"]{${childHTypes.size}}"
    }

}
