package com.arbr.codegen.base.inputs

import java.io.Serializable

data class ArbrPlainObjectField(
    val name: String,
    val description: String,
    val type: ArbrPrimitiveValueType,
    val required: Boolean,
    val codecClass: String,
): Serializable