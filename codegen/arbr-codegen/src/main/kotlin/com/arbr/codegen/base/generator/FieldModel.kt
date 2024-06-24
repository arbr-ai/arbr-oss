package com.arbr.codegen.base.generator

data class FieldModel(
    val qualifiedName: String,
    val tableQualifiedName: String,
    val schemaQualifiedName: String,
    val name: String,
    val type: Class<*>,
    val nullable: Boolean,
    val isPrimaryKey: Boolean?,
    val isVectorEmbeddingKey: Boolean,
    val foreignKeyReference: ForeignKeyReferenceModel?,
    val jsonSchema: String,
)