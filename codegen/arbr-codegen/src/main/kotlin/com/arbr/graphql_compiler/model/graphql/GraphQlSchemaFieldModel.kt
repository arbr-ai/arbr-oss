package com.arbr.graphql_compiler.model.graphql

data class GraphQlSchemaFieldModel(
    val name: String,
    val description: String,
    val typeName: String,
    val directives: List<GraphQlSchemaFieldDirectiveModel>,
)
