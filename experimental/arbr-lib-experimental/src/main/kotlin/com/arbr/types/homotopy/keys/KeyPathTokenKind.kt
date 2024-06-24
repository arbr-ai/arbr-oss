package com.arbr.types.homotopy.keys

sealed class KeyPathTokenKind(val displayCode: String?) {
    data object List : KeyPathTokenKind("[]")
    data object Map : KeyPathTokenKind("#")
    data object Nullable : KeyPathTokenKind("?")
    data class PropertyElement(val key: String, val ordinal: Int) : KeyPathTokenKind(key)
    data class ResourceBody(val key: String) : KeyPathTokenKind("!$key")
    data object NodeChildren : KeyPathTokenKind("->")
    data object NodeValue : KeyPathTokenKind("$")
    data class Primitive(val key: String) : KeyPathTokenKind(":$key")
}
