package com.arbr.codegen.base.generator

data class DisplayForeignRecordModel(
    val index: String,
    val schemaTitleName: String,
    val tableTitleName: String,
    val fieldTitleName: String,
    val schemaConstantName: String,
    val tableConstantName: String,
    val fieldConstantName: String,
    val plainIndex: String,
    val resourcePropertyName: String,
    val resourcePropertyType: String,
    val resourcePropertyQualifiedType: String,
    val resourcePropertyPartialType: String,
    val titleName: String,
)