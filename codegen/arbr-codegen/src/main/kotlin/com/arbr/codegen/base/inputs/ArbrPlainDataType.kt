package com.arbr.codegen.base.inputs

import java.io.Serializable

data class ArbrPlainDataType(
    val name: String,
    val description: String,
    val ordinal: Int,
    val fields: List<ArbrPlainObjectField>,
    val parentReference: ArbrPlainObjectReference?,
    val relations: List<ArbrPlainObjectReference>,
): Serializable
