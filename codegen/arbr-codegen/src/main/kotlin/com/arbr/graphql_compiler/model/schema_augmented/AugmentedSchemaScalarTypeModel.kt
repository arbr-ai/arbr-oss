package com.arbr.graphql_compiler.model.schema_augmented

data class AugmentedSchemaScalarTypeModel(
    val name: String,
    val directives: MutableList<AugmentedSchemaTypeDirectiveModel>,
)