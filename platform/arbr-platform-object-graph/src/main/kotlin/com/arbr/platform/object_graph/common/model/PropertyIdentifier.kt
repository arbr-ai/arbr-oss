package com.arbr.og.object_model.common.model

interface PropertyIdentifier : PropertyDetachedIdentifier, ResourceIdentifier, CompoundPropertyIdentifier {

}

sealed interface CompoundPropertyIdentifier {
    data class ListOf(
        val innerPropertyIdentifier: CompoundPropertyIdentifier
    ): CompoundPropertyIdentifier {
        override fun toString(): String {
            return "List[$innerPropertyIdentifier]"
        }
    }

    data class UnionOf(
        val innerPropertyIdentifier0: CompoundPropertyIdentifier,
        val innerPropertyIdentifier1: CompoundPropertyIdentifier,
    ): CompoundPropertyIdentifier {
        override fun toString(): String {
            return "Union[$innerPropertyIdentifier0, $innerPropertyIdentifier1]"
        }
    }

    data class IndexedAt(
        val innerPropertyIdentifier: CompoundPropertyIdentifier,
        val index: Int,
    ): CompoundPropertyIdentifier {
        override fun toString(): String {
            return "$innerPropertyIdentifier[$index]"
        }
    }

    fun indexedAt(i: Int): CompoundPropertyIdentifier {
        return IndexedAt(this, i)
    }
}
