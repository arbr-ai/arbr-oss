package com.arbr.codegen.base.inputs

import com.arbr.codegen.base.dependencies.GenericObjectMapper
import com.arbr.codegen.base.generator.*
import com.arbr.codegen.base.json_schema.DataTypeJsonField
import com.arbr.codegen.base.json_schema.DataTypeJsonSchema

/**
 * Utility to render build config into code generation input models
 * This process could conceivably have its own configuration in the future, e.g. type mappings
 */
class ArbrObjectModelGenerationInputRenderer(
    private val mapper: GenericObjectMapper
) {

    private fun renderObjectFieldModel(
        domain: String,
        dataType: ArbrPlainDataType,
        field: ArbrPlainObjectField,
    ): FieldModel {
        val dataTypeName = dataType.name
        val name = field.name
        val dataTypeQualifiedName = "$domain.$dataTypeName"
        val qualifiedName = "$dataTypeQualifiedName.$name"
        val type = field.type.valueClass
        val required = field.required

        val arbrObjectField = DataTypeJsonField(
            name = field.name,
            valueType = field.type,
            required = field.required,
            description = field.description,
        )
        val jsonSchema = DataTypeJsonSchema.build {
            value(arbrObjectField)
        }
        val jsonSchemaString = mapper.writeValueAsString(jsonSchema)
        val jsonSchemaEscapedString = mapper.writeValueAsString(jsonSchemaString)

        return FieldModel(
            qualifiedName = qualifiedName,
            tableQualifiedName = dataTypeQualifiedName,
            schemaQualifiedName = domain,
            name = name,
            type = type,
            nullable = !required,
            isPrimaryKey = null, // TODO
            isVectorEmbeddingKey = false,
            foreignKeyReference = null,
            jsonSchema = jsonSchemaEscapedString,
        )
    }

    private fun renderReferenceFieldModel(
        domain: String,
        dataType: ArbrPlainDataType,
        reference: ArbrPlainObjectReference,
        isParentReference: Boolean,
    ): FieldModel {
        val dataTypeName = dataType.name
        val name = reference.name
        val dataTypeQualifiedName = "$domain.$dataTypeName"
        val qualifiedName = "$dataTypeQualifiedName.$name"

        val referenceDataTypeName = reference.targetDataTypeName
        val referencePrimaryKey = RESOURCE_ID_FIELD_NAME
        val foreignFieldQualifiedName = "$domain.${referenceDataTypeName}.$referencePrimaryKey"

        val arbrObjectField = DataTypeJsonField(
            name = name,
            valueType = ArbrPrimitiveValueType.STRING,
            required = false,
            description = reference.description,
        )
        val jsonSchema = DataTypeJsonSchema.build {
            value(arbrObjectField)
        }
        val jsonSchemaString = mapper.writeValueAsString(jsonSchema)
        val jsonSchemaEscapedString = mapper.writeValueAsString(jsonSchemaString)

        // Only parent references are allowed to be required at the moment
        val required = isParentReference

        return FieldModel(
            qualifiedName = qualifiedName,
            tableQualifiedName = dataTypeQualifiedName,
            schemaQualifiedName = domain,
            name = name,
            type = String::class.java,
            nullable = !required,
            isPrimaryKey = false,
            isVectorEmbeddingKey = false,
            foreignKeyReference = ForeignKeyReferenceModel(
                foreignFieldQualifiedName
            ),
            jsonSchema = jsonSchemaEscapedString,
        )
    }

    private fun renderTableModel(
        inboundReferenceKeyFieldQualifiedNameMap: Map<String, List<Pair<String, String>>>,
        domain: String,
        dataType: ArbrPlainDataType,
    ): TableModel {
        val name = dataType.name
        val qualifiedName = "$domain.$name"

        val objectFields = dataType.fields
            .map { renderObjectFieldModel(domain, dataType, it) }

        val parentReferenceFields = listOfNotNull(
            dataType.parentReference?.let {
                renderReferenceFieldModel(domain, dataType, it, true)
            }
        )
        val referenceFields = dataType.relations.map {
            renderReferenceFieldModel(domain, dataType, it, false)
        }

        return TableModel(
            qualifiedName = qualifiedName,
            schemaQualifiedName = domain,
            schemaName = domain,
            name = name,
            fields = objectFields + parentReferenceFields + referenceFields,
            primaryKeyFieldQualifiedName = null,
            inboundReferenceKeyFieldQualifiedNames = inboundReferenceKeyFieldQualifiedNameMap.getOrDefault(
                dataType.name,
                emptyList()
            ),
        )
    }

    /**
     * Return map of dataType.name to list of pairs (tableQualifiedName, fieldQualifiedName) referencing table as inbound
     */
    private fun getInboundReferenceKeyFieldQualifiedNameMap(
        domain: String,
        dataTypes: List<ArbrPlainDataType>,
    ): Map<String, List<Pair<String, String>>> {
        return dataTypes
            .flatMap { dataType ->
                val allRelations = listOfNotNull(dataType.parentReference) + dataType.relations

                allRelations.map { relation ->
                    val dataTypeQualifiedName = "$domain.${dataType.name}"
                    val qualifiedName = "$dataTypeQualifiedName.${relation.name}"

                    relation.targetDataTypeName to (dataTypeQualifiedName to qualifiedName)
                }
            }
            .groupBy { it.first }
            .mapValues { it.value.map { p -> p.second } }
    }

    fun render(
        domain: String,
        dataTypes: List<ArbrPlainDataType>,
    ): DatabaseModel {
        val inboundMap = getInboundReferenceKeyFieldQualifiedNameMap(domain, dataTypes)

        // Single-schema in "database"
        return DatabaseModel(
            listOf(
                SchemaModel(
                    domain,
                    domain,
                    dataTypes
                        .map { renderTableModel(inboundMap, domain, it) }
                )
            )
        )
    }

    companion object {
        const val RESOURCE_ID_FIELD_NAME = "uuid"
    }

}