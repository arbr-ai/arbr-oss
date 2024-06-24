package com.arbr.types.homotopy.keys

sealed interface PropertyKey {
    val name: String
    val ordinal: Int

    companion object {
        fun of(name: String, ordinal: Int): PropertyKey {
            return EnumPropertyKey(name, ordinal)
        }

        fun <E: Enum<E>> of(e: E): PropertyKey {
            return EnumPropertyKey(e.name, e.ordinal)
        }

        private data class EnumPropertyKey(
            override val name: String,
            override val ordinal: Int,
        ) : PropertyKey
    }
}
