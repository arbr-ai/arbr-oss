package com.arbr.codegen.base.generator

data class DisplayFieldModelAny(
    val simplePropertyField: List<DisplayFieldModel>,
)

data class DisplayReferenceFieldModelAny(
    val outboundReferenceField: List<DisplayFieldModel>,
)

data class DisplayForeignRecordModelAny(
    val foreignRecord: List<DisplayForeignRecordModel>,
)

data class DisplayTableModel(
    val titleName: String,
    val schemaTitleName: String,
    val constantName: String,
    val field: List<DisplayFieldModel>,
    val anySimplePropertyField: List<DisplayFieldModelAny>,
    val simplePropertyField: List<DisplayFieldModel>,
    val anyOutboundReferenceField: List<DisplayReferenceFieldModelAny>,
    val outboundReferenceField: List<DisplayFieldModel>,
    val anyForeignRecord: List<DisplayForeignRecordModelAny>,
    val foreignRecord: List<DisplayForeignRecordModel>,
    val foreignTypeRecord: List<DisplayForeignTableModel>,
    val propertyName: String,
    val snakeName: String,
) {

    val pTableTitleName: String = titleName
    val pTableConstantName: String = constantName

}