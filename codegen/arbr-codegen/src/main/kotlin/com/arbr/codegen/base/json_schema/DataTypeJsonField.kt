package com.arbr.codegen.base.json_schema

import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType

data class DataTypeJsonField(
    val name: String,
    val valueType: ArbrPrimitiveValueType,
    val required: Boolean,
    val description: String?,
)