package com.arbr.codegen.base.inputs

import java.io.Serializable

enum class ArbrPrimitiveValueType(val valueClass: Class<*>): Serializable {
    BOOLEAN(Boolean::class.java),
    INTEGER(Int::class.java),
    LONG(Long::class.java),
    FLOAT(Float::class.java),
    DOUBLE(Double::class.java),
    STRING(String::class.java),
}