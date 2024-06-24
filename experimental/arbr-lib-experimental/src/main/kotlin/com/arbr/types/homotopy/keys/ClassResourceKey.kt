package com.arbr.types.homotopy.keys

abstract class ClassResourceKey(
    override val clazz: Class<*>
): ResourceKey {
    override fun toString(): String {
        return key
    }
}