package com.arbr.types.homotopy

import com.arbr.types.homotopy.keys.KeyPathTokenKind

interface MutablePathContext: PathContext {
    override val pathTokens: MutableList<KeyPathTokenKind>
    override val contextMap: MutableMap<String, Any>

    fun <T> with(token: KeyPathTokenKind, f: () -> T) : T {
        pathTokens.add(token)
        return f().also { pathTokens.removeLast() }
    }

    companion object {
        private class MutablePathContextImpl(
            override val pathTokens: MutableList<KeyPathTokenKind>,
            override val contextMap: MutableMap<String, Any>,
        ): MutablePathContext

        fun new(): MutablePathContext {
            return MutablePathContextImpl(mutableListOf(), mutableMapOf())
        }
    }
}