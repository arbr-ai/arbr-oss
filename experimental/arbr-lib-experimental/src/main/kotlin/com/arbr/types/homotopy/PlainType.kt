package com.arbr.types.homotopy

import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

sealed class PlainType {
    data class Leaf(val kType: KType) : PlainType() {
        override fun toString(): String {
            return "Leaf[${kType.jvmErasure.simpleName ?: "*"}]"
        }
    }
    data class Ref(val kType: KType) : PlainType() {
        override fun toString(): String {
            return "Ref[${kType.jvmErasure.simpleName ?: "*"}]"
        }
    }
    data class NullableOf(val inner: PlainType) : PlainType() {
        override fun toString(): String {
            return "Nl[${inner}]"
        }
    }
    data class ListOf(val inner: PlainType) : PlainType() {
        override fun toString(): String {
            return "List[${inner}]"
        }
    }
    data class MapOf(val innerKey: PlainType, val innerValue: PlainType) : PlainType() {
        override fun toString(): String {
            return "Map[${innerKey}, ${innerValue}]"
        }
    }
}