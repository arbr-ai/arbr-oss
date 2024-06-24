package com.arbr.codegen.base.generator

data class DisplaySchemaModel(
    val titleName: String,
    val constantName: String,
    val name: String,
    val table: List<DisplayTableModel>,
) {

    val pSchemaTitleName: String = titleName
    val pSchemaConstantName: String = constantName

}
