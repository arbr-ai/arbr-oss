package com.arbr.codegen.base.generator

import com.arbr.codegen.base.dependencies.GenericObjectMapper
import com.arbr.codegen.base.util.StringUtils

class DisplayModelConverter(
    private val mapper: GenericObjectMapper,
) {

    private fun getFieldName(field: FieldModel) =
        if (field.foreignKeyReference != null && field.name.endsWith("_id")) {
            field.name.dropLast(3)
        } else {
            field.name
        }

    private fun schemaDisplayModel(
        databaseModel: DatabaseModel,
        schema: SchemaModel,
    ): DisplaySchemaModel {
        val schemaNameSuite = StringUtils.getCaseSuite(schema.name)
        val schemaTitleName = schemaNameSuite.titleCase

        return DisplaySchemaModel(
            schemaTitleName,
            schemaNameSuite.screamingSnakeCase,
            schemaNameSuite.camelCase,
            schema.tables.map { table ->
                val tableNameSuite = StringUtils.getCaseSuite(table.name)

                val tableConstantName = tableNameSuite.screamingSnakeCase
                val tableTitleName = tableNameSuite.titleCase
                val displayFields = table.fields.mapIndexed { i, field ->
                    val fieldNameSuite = StringUtils.getCaseSuite(getFieldName(field))
                    val constantName = fieldNameSuite.screamingSnakeCase

                    val embeddingKey = if (field.isVectorEmbeddingKey) {
                        mapper.writeValueAsString(field.qualifiedName)
                    } else {
                        "null"
                    }

                    val typeName = if (field.nullable) {
                        field.type.kotlin.simpleName!! + "?"
                    } else {
                        field.type.kotlin.simpleName!!
                    }
                    val typeNameNonNull = field.type.kotlin.simpleName!!

                    val nullableMark = if (field.nullable) "?" else ""
                    val required = if (field.nullable) "false" else "true"

                    val fieldTitleName = fieldNameSuite.titleCase
                    val pvsType = if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                        "$schemaTitleName$tableTitleName${fieldTitleName}Value"
                    } else {
                        val foreignField =
                            databaseModel.getField(field.foreignKeyReference.foreignFieldQualifiedName)

                        val foreignTable = databaseModel.getTable(foreignField.tableQualifiedName)
                        val foreignTableNameSuite = StringUtils.getCaseSuite(foreignTable.name)

                        val foreignSchema = databaseModel.getSchema(foreignTable.schemaQualifiedName)
                        val foreignSchemaNameSuite = StringUtils.getCaseSuite(foreignSchema.name)

                        "ObjectRef<out ${foreignSchemaNameSuite.titleCase}${
                            foreignTableNameSuite.titleCase
                        }, Partial${
                            foreignTableNameSuite.titleCase
                        }, ${foreignSchemaNameSuite.titleCase}ForeignKey>"
                    }

                    val partialType = if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                        "$schemaTitleName${tableTitleName}${fieldTitleName}Value"
                    } else {
                        val foreignField =
                            databaseModel.getField(field.foreignKeyReference.foreignFieldQualifiedName)
                        val foreignTable = databaseModel.getTable(foreignField.tableQualifiedName)
                        val foreignTableNameSuite = StringUtils.getCaseSuite(foreignTable.name)

                        foreignTableNameSuite.titleCase
                    }

                    val jsonValue = fieldNameSuite.camelCase

                    val partialIntoModelValue =
                        if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                            fieldNameSuite.camelCase
                        } else {
                            fieldNameSuite.camelCase + "?.objectRef()"
                        }

                    val (fieldSchemaTitle, fieldTableTitle) = if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                        schemaTitleName to tableTitleName
                    } else {
                        val foreignField =
                            databaseModel.getField(field.foreignKeyReference.foreignFieldQualifiedName)
                        val foreignTable = databaseModel.getTable(foreignField.tableQualifiedName)
                        val foreignTableNameSuite = StringUtils.getCaseSuite(foreignTable.name)
                        val foreignSchema = databaseModel.getSchema(foreignTable.schemaQualifiedName)
                        val foreignSchemaNameSuite = StringUtils.getCaseSuite(foreignSchema.name)

                        foreignSchemaNameSuite.titleCase to foreignTableNameSuite.titleCase
                    }

                    val relationship = if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                        "PROPERTY"
                    } else {
                        "PARENT"
                    }

                    val resourcePropertyName = fieldNameSuite.camelCase
                    val propertyIdentifierName =
                        if (field.foreignKeyReference == null || field.isVectorEmbeddingKey) {
                            resourcePropertyName
                        } else {
                            "$schemaTitleName.${tableTitleName}.${fieldTitleName}"
                        }


                    DisplayFieldModel(
                        index = i.toString(),
                        titleName = fieldTitleName,
                        valueType = typeName,
                        valueTypeNonNull = typeNameNonNull,
                        dbType = typeName,
                        constantName = constantName,
                        embeddingKey = embeddingKey,
                        resourcePropertyName = resourcePropertyName,
                        simplePropertyName = fieldNameSuite.camelCase,
                        nullableMark = nullableMark,
                        required = required,
                        pvsType = pvsType,
                        partialType = partialType,
                        jsonValue = jsonValue,
                        partialIntoModelValue = partialIntoModelValue,
                        schemaTitle = fieldSchemaTitle,
                        tableTitle = fieldTableTitle,
                        relationship = relationship,
                        propertyIdentifierName = propertyIdentifierName,
                        jsonSchema = field.jsonSchema,
                        reflexiveReferentPropertyName = "",
                    )
                }

                val simplePropertyFields = displayFields.zip(table.fields)
                    .filter { (_, field) ->
                        (field.isVectorEmbeddingKey || field.foreignKeyReference == null)
                    }
                    .map { it.first }
                val anySimplePropertyField = if (simplePropertyFields.isEmpty()) emptyList() else listOf(
                    DisplayFieldModelAny(simplePropertyFields)
                )

                val baseOutboundReferences = displayFields.zip(table.fields)
                    .filter { (_, field) ->
                        (!field.isVectorEmbeddingKey && field.foreignKeyReference != null)
                    }
                val outboundTableReferenceCount = baseOutboundReferences
                    .map { it.first }
                    .groupBy { it.tableTitle }
                    .mapValues { it.value.count() }

                val outboundReferenceFields = baseOutboundReferences
                    .map { (displayFieldModel, field) ->
                        val referringFieldNameSuite = StringUtils.getCaseSuite(getFieldName(field))
                        val resourcePropertyName =
                            if (outboundTableReferenceCount.getOrDefault(displayFieldModel.tableTitle, 0) > 1) {
                                referringFieldNameSuite.camelCase + "Of" + tableNameSuite.titleCase
                            } else {
                                tableNameSuite.camelCase + "s"
                            }

                        displayFieldModel.copy(
                            reflexiveReferentPropertyName = resourcePropertyName,
                        )
                    }

                val numInboundKeysFromTable = table.inboundReferenceKeyFieldQualifiedNames
                    .groupBy { it.first }
                    .mapValues { it.value.size }

                val foreignRecord =
                    table.inboundReferenceKeyFieldQualifiedNames.mapIndexed { j, (tableQualifiedName, fieldQualifiedName) ->
                        val numInboundFromTable = numInboundKeysFromTable[tableQualifiedName]!!

                        val childTable = databaseModel.getTable(tableQualifiedName)
                        val childTableNameSuite = StringUtils.getCaseSuite(childTable.name)

                        val childSchema = databaseModel.getSchema(childTable.schemaQualifiedName)
                        val childSchemaNameSuite = StringUtils.getCaseSuite(childSchema.name)

                        val referringField = databaseModel.getField(fieldQualifiedName)

                        val typeName = if (childTable.schemaQualifiedName != table.schemaQualifiedName) {
                            childSchemaNameSuite.titleCase + childTableNameSuite.titleCase
                        } else {
                            childSchemaNameSuite.titleCase + childTableNameSuite.titleCase
                        }

                        val qualifiedTypeName =
                            childSchemaNameSuite.titleCase + childTableNameSuite.titleCase
                        val qualifiedPartialTypeName = "Partial" + childTableNameSuite.titleCase

                        val referringFieldName = getFieldName(referringField)
                        val referringFieldNameSuite = StringUtils.getCaseSuite(referringFieldName)

                        // Qualify child (inbound foreign) property names only if there are multiple from the
                        // same table since it's a minority case
                        val resourcePropertyName: String
                        val titleName: String
                        if (numInboundFromTable > 1) {
                            resourcePropertyName =
                                referringFieldNameSuite.camelCase + "Of" + childTableNameSuite.titleCase
                            titleName =
                                referringFieldNameSuite.titleCase + "Of" + childTableNameSuite.titleCase
                        } else {
                            resourcePropertyName = childTableNameSuite.camelCase + "s"
                            titleName = childTableNameSuite.titleCase
                        }

                        val schemaTitleName = childSchemaNameSuite.titleCase
                        val childTableTitleName = childTableNameSuite.titleCase
                        val fieldTitleName = referringFieldNameSuite.titleCase

                        val schemaConstantName = childSchemaNameSuite.screamingSnakeCase
                        val tableConstantName = childTableNameSuite.screamingSnakeCase
                        val fieldConstantName = referringFieldNameSuite.screamingSnakeCase

                        DisplayForeignRecordModel(
                            (table.fields.size + j).toString(),
                            schemaTitleName,
                            childTableTitleName,
                            fieldTitleName,

                            schemaConstantName,
                            tableConstantName,
                            fieldConstantName,

                            j.toString(),
                            resourcePropertyName,
                            typeName,
                            qualifiedTypeName,
                            qualifiedPartialTypeName,
                            titleName
                        )
                    }

                // Records for foreign types, i.e. grouped by table
                val foreignTypeRecord =
                    foreignRecord.groupBy { it.tableTitleName }
                        .map {
                            DisplayForeignTableModel(
                                it.key,
                                StringUtils.getCaseSuite(it.key).camelCase,
                                it.value,
                            )
                        }

                val anyOutboundReferenceField = if (outboundReferenceFields.isEmpty()) emptyList() else listOf(
                    DisplayReferenceFieldModelAny(outboundReferenceFields)
                )
                val anyForeignRecord = if (foreignRecord.isEmpty()) emptyList() else listOf(
                    DisplayForeignRecordModelAny(foreignRecord)
                )

                DisplayTableModel(
                    tableTitleName,
                    schemaTitleName,
                    tableConstantName,
                    displayFields,
                    anySimplePropertyField,
                    simplePropertyFields,
                    anyOutboundReferenceField,
                    outboundReferenceFields,
                    anyForeignRecord,
                    foreignRecord,
                    foreignTypeRecord,
                    tableNameSuite.camelCase,
                    tableNameSuite.snakeCase,
                )
            }
        )
    }

    /**
     * Filter display model=remove items that should not be rendered via the template, such as the Root datatype and
     * the UUID property. This has=be done immediately before generation=allow references=these items earlier.
     */
    private fun filterDisplayModel(
        displayRootModel: DisplayRootModel
    ): DisplayRootModel {
        val newSchema = displayRootModel.schema.map { displaySchemaModel ->
            displaySchemaModel.copy(
                table = displaySchemaModel.table.filter { displayTableModel ->
//                    displayTableModel.propertyName != ROOT_TABLE_NAME
                    true
                }
                    .map { displayTableModel ->
                        displayTableModel.copy(
                            field = displayTableModel.field.filter { displayFieldModel ->
                                displayFieldModel.simplePropertyName != UUID_FIELD_NAME
                            },
                            simplePropertyField = displayTableModel.simplePropertyField.filter { displayFieldModel ->
                                displayFieldModel.simplePropertyName != UUID_FIELD_NAME
                            },
                            anySimplePropertyField = displayTableModel.anySimplePropertyField.mapNotNull { anySimplePropertyField ->
                                val filteredSimplePropertyField =
                                    anySimplePropertyField.simplePropertyField.filter { displayFieldModel ->
                                        displayFieldModel.simplePropertyName != UUID_FIELD_NAME
                                    }

                                if (filteredSimplePropertyField.isEmpty()) {
                                    null
                                } else {
                                    anySimplePropertyField.copy(
                                        simplePropertyField = filteredSimplePropertyField,
                                    )
                                }
                            },
                            outboundReferenceField = displayTableModel.outboundReferenceField.filter { displayFieldModel ->
                                displayFieldModel.simplePropertyName != UUID_FIELD_NAME
                            },
                        )
                    }
            )
        }

        return displayRootModel.copy(schema = newSchema)
    }

    internal fun databaseDisplayModel(
        packageDomain: String,
        databaseModel: DatabaseModel,
    ): DisplayRootModel {
        val schemaDisplayModels = databaseModel.schemata.map {
            DisplayModelConverter(
                mapper
            ).schemaDisplayModel(
                databaseModel,
                it,
            )
        }
        return DisplayRootModel(
            packageDomain,
            schemaDisplayModels,
            sourcedStructModels(SOURCED_STRUCT_MAX_SIZE),
        ).run(this::filterDisplayModel)
    }

    companion object {
        private const val ROOT_TABLE_NAME = "root"
        private const val UUID_FIELD_NAME = "uuid"
        private const val SOURCED_STRUCT_MAX_SIZE = 64

        private fun sourcedStructModels(maxSize: Int): List<StructDataModel> {
            return (1..maxSize).map { size ->
                StructDataModel(
                    size = size,
                    generic = (1..size).map { index ->
                        StructGenericDataModel(
                            index = index,
                            last = (index == size),
                        )
                    },
                    field = (1..size).map { index ->
                        StructFieldDataModel(
                            index = index,
                            indexZero = index - 1,
                            last = (index == size),
                        )
                    }
                )
            }
        }
    }
}

