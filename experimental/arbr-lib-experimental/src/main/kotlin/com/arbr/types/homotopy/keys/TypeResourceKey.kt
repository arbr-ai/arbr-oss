package com.arbr.types.homotopy.keys

import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

class TypeResourceKey(
    val type: KType,
): ClassResourceKey(type.jvmErasure.java) {
    override fun toString(): String {
        return key
    }
}