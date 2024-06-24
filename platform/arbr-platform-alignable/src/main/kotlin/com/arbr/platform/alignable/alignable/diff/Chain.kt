package com.arbr.platform.alignable.alignable.diff

data class Chain<T>(
    val parent: Chain<T>?,
    val node: T?,
    val hashCode: Int,
) {
    fun and(child: T): Chain<T> {
        return Chain(
            this,
            child,
            (this.hashCode() * 31) + hashCode()
        )
    }

    private fun toMutableList(): MutableList<T> {
        return (parent?.toMutableList() ?: mutableListOf())
            .also {
                if (node != null) {
                    it.add(node)
                }
            }
    }

    fun toList(): List<T> {
        return toMutableList()
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is Chain<*> && other.hashCode == hashCode
    }
}
