package com.arbr.graphql_compiler.model.schema_augmented

data class AugmentedSchemaTypeModel(
    val name: String,
    val description: String,
    val fields: MutableList<AugmentedSchemaFieldModel>,
)
