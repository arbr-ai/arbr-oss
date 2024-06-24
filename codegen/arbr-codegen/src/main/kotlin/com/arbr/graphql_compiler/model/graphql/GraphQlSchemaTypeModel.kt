package com.arbr.graphql_compiler.model.graphql

data class GraphQlSchemaTypeModel(
    val name: String,
    val description: String,
    val fields: List<GraphQlSchemaFieldModel>,
)