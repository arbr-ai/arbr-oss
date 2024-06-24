package com.arbr.codegen.base.generator

data class DisplayRootModel(
    val packageDomain: String,
    val schema: List<DisplaySchemaModel>,

    /**
     * SourcedStruct data model
     */
    val struct: List<StructDataModel>,
)
