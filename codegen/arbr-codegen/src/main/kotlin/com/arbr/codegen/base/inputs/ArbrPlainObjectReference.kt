package com.arbr.codegen.base.inputs

import java.io.Serializable

data class ArbrPlainObjectReference(
    val name: String,
    val description: String,
    val targetDataTypeName: String,
): Serializable