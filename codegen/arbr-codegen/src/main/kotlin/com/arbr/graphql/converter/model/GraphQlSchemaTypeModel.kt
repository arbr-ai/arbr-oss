package com.arbr.graphql.converter.model

data class GraphQlSchemaTypeModel(
    val name: String,
    val fields: List<GraphQlSchemaFieldModel>,
)