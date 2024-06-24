package com.arbr.codegen.base.generator

data class SchemaModel(
    val qualifiedName: String,
    val name: String,
    val tables: List<TableModel>,
)