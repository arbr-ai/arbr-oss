package com.arbr.codegen.base.generator

data class StructDataModel(
    val size: Int,
    val generic: List<StructGenericDataModel>,
    val field: List<StructFieldDataModel>,
)