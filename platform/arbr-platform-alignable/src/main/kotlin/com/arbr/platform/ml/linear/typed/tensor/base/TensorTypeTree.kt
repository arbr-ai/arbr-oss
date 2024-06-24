package com.arbr.platform.ml.linear.typed.tensor.base

import java.util.*

sealed class TensorTypeTree {

    abstract fun getDescriptor(): String

    data class ProductOf(
        val leftNode: TensorTypeTree,
        val rightNode: TensorTypeTree,
    ) : TensorTypeTree() {
        fun associate(): List<TensorTypeTree> {
            val associatedTypes = LinkedList<TensorTypeTree>(listOf(this))

            var earliestReplacement = 0
            while (true) {
                var i = associatedTypes.size - 1
                val replacements = mutableListOf<Pair<Int, ProductOf>>()
                for (innerTypeTree in associatedTypes.descendingIterator()) {
                    if (i < earliestReplacement) {
                        break
                    }

                    if (innerTypeTree is ProductOf) {
                        replacements.add(i to innerTypeTree)
                    }

                    i--
                }

                if (replacements.isEmpty()) {
                    break
                } else {
                    earliestReplacement = replacements.last().first
                    for ((j, st) in replacements) {
                        associatedTypes.removeAt(j)
                        associatedTypes.addAll(j, listOf(st.leftNode, st.rightNode))
                    }
                }
            }

            return associatedTypes
        }

        override fun getDescriptor(): String {
            return associate().joinToString(" x ") {
                it.getDescriptor()
            }
        }
    }

    data class SumOf(
        val leftNode: TensorTypeTree,
        val rightNode: TensorTypeTree,
    ) : TensorTypeTree() {
        fun associate(): List<TensorTypeTree> {
            val associatedTypes = LinkedList<TensorTypeTree>(listOf(this))

            var earliestReplacement = 0
            while (true) {
                var i = associatedTypes.size - 1
                val replacements = mutableListOf<Pair<Int, SumOf>>()
                for (innerTypeTree in associatedTypes.descendingIterator()) {
                    if (i < earliestReplacement) {
                        break
                    }

                    if (innerTypeTree is SumOf) {
                        replacements.add(i to innerTypeTree)
                    }

                    i--
                }

                if (replacements.isEmpty()) {
                    break
                } else {
                    earliestReplacement = replacements.last().first
                    for ((j, st) in replacements) {
                        associatedTypes.removeAt(j)
                        associatedTypes.addAll(j, listOf(st.leftNode, st.rightNode))
                    }
                }
            }

            return associatedTypes
        }

        override fun getDescriptor(): String {
            val innerString = associate().joinToString(", ") {
                it.getDescriptor()
            }
            return "{$innerString}"
        }
    }

    data class Leaf(val baseIdentifier: String) : TensorTypeTree() {

        override fun getDescriptor(): String {
            return baseIdentifier
        }
    }
}
