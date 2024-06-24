package com.arbr.codegen.target.compiler.dsl

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.codegen.base.inputs.ArbrPlainObjectField
import com.arbr.codegen.base.inputs.ArbrPlainObjectReference
import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType
import com.arbr.graphql.lang.common.AppendableBlock
import com.arbr.graphql.lang.common.GraphQlNodeRenderer
import com.arbr.graphql.lang.node.*
import com.arbr.graphql_compiler.util.StringUtils
import org.slf4j.LoggerFactory
import java.io.Writer

class ArbrObjectModelNodeProcessor: GraphQlNodeRenderer() {
    override val writer: Writer
        get() = Writer.nullWriter()

    private val mapper = MapperConfig().mapper

    private val plainDataTypes: MutableList<ArbrPlainDataType> = mutableListOf()

    private fun getArbrType(
        typeName: String,
    ): ArbrPrimitiveValueType? {
        return when (typeName) {
            "String", "StringValue" -> {
                ArbrPrimitiveValueType.STRING
            }
            "Long" -> { // Custom scalar type
                ArbrPrimitiveValueType.LONG
            }
            "Int", "IntValue" -> {
                ArbrPrimitiveValueType.INTEGER
            }
            "Float", "FloatValue" -> {
                ArbrPrimitiveValueType.FLOAT
            }
            "Boolean", "BooleanValue" -> {
                ArbrPrimitiveValueType.BOOLEAN
            }
            "ID", "IDValue" -> {
                ArbrPrimitiveValueType.STRING
            }
            else -> {
                // Reference type
                null
            }
        }
    }

    private fun getArbrType(
        graphQlLanguageNodeType: GraphQlLanguageNodeType,
    ): ArbrPrimitiveValueType? {
        return when (graphQlLanguageNodeType) {
            is GraphQlLanguageNodeListType -> throw NotImplementedError()
            is GraphQlLanguageNodeNonNullType -> throw NotImplementedError()
            is GraphQlLanguageNodeTypeName -> getArbrType(graphQlLanguageNodeType.name!!)
        }
    }

    private fun formatDescription(description: String): String {
        return description
            .replace("\\\"", "\"")
            .trim()
    }

    private fun getArbrObjectReferenceFieldName(graphQlSchemaFieldName: String): String {
        return StringUtils.getCaseSuite(graphQlSchemaFieldName).camelCase
    }

    private fun getArbrObjectFieldName(graphQlSchemaFieldName: String): String {
        return StringUtils.getCaseSuite(graphQlSchemaFieldName).camelCase
    }

    private fun typeNames(type: GraphQlLanguageNodeType): List<String> {
        return when (type) {
            is GraphQlLanguageNodeListType -> typeNames(type.type!!)
            is GraphQlLanguageNodeNonNullType -> typeNames(type.type!!)
            is GraphQlLanguageNodeTypeName -> listOf(type.name!!)
            else -> throw IllegalStateException()
        }
    }

    override fun renderGraphQlLanguageNodeObjectTypeDefinition(node: GraphQlLanguageNodeObjectTypeDefinition): AppendableBlock? {
        val name = node.name
        if (name == null) {
            logger.warn("Skipping type definition missing name")
            return null
        }

        val nodeDescription = formatDescription(node.description?.content ?: "")
        val definitionNameCaseSuite = StringUtils.getCaseSuite(name)

        val fieldDefinitions = node.fieldDefinitions ?: emptyList()
        var parent: ArbrPlainObjectReference? = null
        val simpleFields = mutableListOf(
            // Add required UUID field
            ArbrPlainObjectField(
                "uuid",
                description = "",
                type = ArbrPrimitiveValueType.STRING,
                required = true,
                codecClass = "",
            )
        )
        val refs = mutableListOf<ArbrPlainObjectReference>()

        fieldDefinitions.forEach { fieldDefinition ->
            val fieldName = fieldDefinition.name!!
            val type = fieldDefinition.type!!
            val typeNames = typeNames(type)
            val description = fieldDefinition.description?.content ?: ""

            val objectTypeReferences = typeNames.filter {
                it !in graphQlCustomScalarTypeNames && it !in graphQlPrimitiveTypeNames
            }
            check(objectTypeReferences.size <= 1)

            val objectTypeReference = objectTypeReferences.firstOrNull()
            if (objectTypeReference != null) {
                val referenceField = ArbrPlainObjectReference(
                    getArbrObjectReferenceFieldName(fieldName),
                    description,
                    StringUtils.getCaseSuite(objectTypeReference).camelCase
                )

                if (referenceField.name == "parent") {
                    parent = referenceField
                } else {
                    refs.add(referenceField)
                }
            } else {
                val hasNullableDirective = fieldDefinition.directives?.any { it.name == "nullable" } == true
                val required = !hasNullableDirective
                val codecClass = ""

                val field = ArbrPlainObjectField(
                    getArbrObjectFieldName(fieldName),
                    description,
                    getArbrType(type)!!,
                    required,
                    codecClass
                )
                simpleFields.add(field)
            }
        }

        val dataType = ArbrPlainDataType(
            definitionNameCaseSuite.camelCase,
            nodeDescription,
            plainDataTypes.size,
            simpleFields,
            parent,
            refs,
        )
        plainDataTypes.add(dataType)

        return {
            appendLine(
                mapper.writeValueAsString(dataType)
            )
        }
    }

    fun getDataTypes(): List<ArbrPlainDataType> {
        return plainDataTypes.toList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ArbrObjectModelNodeProcessor::class.java)

        private val graphQlPrimitiveTypeNames = setOf(
            "ID",
            "Int",
            "Float",
            "Boolean",
            "String",
        )
        private val graphQlCustomScalarTypeNames = setOf("Long")
    }
}