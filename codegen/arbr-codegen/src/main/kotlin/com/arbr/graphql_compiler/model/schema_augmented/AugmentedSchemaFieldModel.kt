package com.arbr.graphql_compiler.model.schema_augmented

data class AugmentedSchemaFieldModel(
    val name: String,
    val description: String,
    var typeName: String,
    val directives: MutableList<AugmentedSchemaFieldDirectiveModel>,
)
