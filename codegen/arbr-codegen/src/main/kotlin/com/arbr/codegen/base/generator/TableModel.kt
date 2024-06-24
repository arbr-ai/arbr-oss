package com.arbr.codegen.base.generator

data class TableModel(
    val qualifiedName: String,
    val schemaQualifiedName: String,
    val schemaName: String,
    val name: String,
    val fields: List<FieldModel>,
    val primaryKeyFieldQualifiedName: String?,
    val inboundReferenceKeyFieldQualifiedNames: List<Pair<String, String>>,
)