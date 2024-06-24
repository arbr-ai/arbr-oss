package com.arbr.types.homotopy.keys

interface HTypeElementKey {
    val name: String
}

data class CompoundKeyPathPropertyKey(
    val pathTokens: List<KeyPathTokenKind>,
) : HTypeElementKey {

    override val name: String = pathTokens.joinToString("") { it.displayCode ?: "?" }

    override fun toString(): String {
        return name
    }
}
