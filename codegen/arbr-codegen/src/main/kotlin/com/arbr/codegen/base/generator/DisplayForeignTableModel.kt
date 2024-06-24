package com.arbr.codegen.base.generator

data class DisplayForeignTableModel(
    val titleName: String,
    val resourcePropertyName: String,
    val foreignRecord: List<DisplayForeignRecordModel>,
)