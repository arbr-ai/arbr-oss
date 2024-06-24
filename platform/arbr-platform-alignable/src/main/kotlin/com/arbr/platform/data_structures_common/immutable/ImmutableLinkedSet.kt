package com.arbr.platform.data_structures_common.immutable

// TODO: Deprecate this
class ImmutableLinkedSet<K> private constructor(
    private val innerSet: Set<K>
) : Set<K> by innerSet {

    constructor(
        values: Collection<K>
    ): this(
        // Transitional shortcut before tearing this class down
        if (values is Set) {
            values
        } else {
            values.toSet()
        }
    )

    override fun toString(): String {
        return "{${joinToString(", ") { it.toString() }}}"
    }

}
