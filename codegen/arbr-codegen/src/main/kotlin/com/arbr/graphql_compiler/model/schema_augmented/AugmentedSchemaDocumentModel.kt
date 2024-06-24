package com.arbr.graphql_compiler.model.schema_augmented

data class AugmentedSchemaDocumentModel(
    val types: MutableList<AugmentedSchemaTypeModel>,
    val scalarTypes: MutableList<AugmentedSchemaScalarTypeModel>,
)
