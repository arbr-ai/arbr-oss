package com.arbr.graphql_compiler.util

fun interface AppendableTarget {

    fun appendWith(appendable: Appendable): Appendable

    companion object {

        fun with(f: Appendable.() -> Unit): AppendableTarget {
            return AppendableTarget { appendable ->
                f(appendable)
                appendable
            }
        }
    }
}