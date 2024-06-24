package com.arbr.types.homotopy

import com.arbr.types.homotopy.keys.KeyPathTokenKind

interface PathContext {
    val pathTokens: List<KeyPathTokenKind>
    val contextMap: Map<String, Any>

    val pathString: String
        get() = pathTokens.joinToString("") { it.displayCode ?: "?" }

    companion object {
        private class PathContextImpl(
            override val pathTokens: List<KeyPathTokenKind>,
            override val contextMap: Map<String, Any>,
        ): PathContext

        fun new(): PathContext {
            return PathContextImpl(listOf(), mapOf())
        }
    }
}