package com.arbr.codegen.target.converter.dsl

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.backdraft.target.GraphQlConverter
import com.arbr.backdraft.task.handler.DefaultCompilationTaskHandler
import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.codegen.base.inputs.ArbrPlainObjectField
import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType
import com.arbr.codegen.base.util.StringUtils
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import com.arbr.graphql_compiler.component.compiler.impl.schema_merge.SchemaRenderingNodeProcessor
import com.arbr.graphql_compiler.core.SchemaFileCollectionSource
import com.arbr.graphql_compiler.core.SchemaSource
import com.arbr.graphql_compiler.model.graphql.GraphQlSchemaDocumentModel
import com.arbr.graphql_compiler.model.graphql.GraphQlSchemaFieldDirectiveModel
import com.arbr.graphql_compiler.model.graphql.GraphQlSchemaFieldModel
import com.arbr.graphql_compiler.model.graphql.GraphQlSchemaTypeModel
import com.arbr.graphql_compiler.util.topSortWith
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter

class ArbrToGraphQlSchemaConverter(
    private val outputDir: Path,
): GraphQlConverter {
    private val mapper = jacksonObjectMapper()

    private val outputWriter: Writer = Paths.get(
        outputDir.toString(),
        OUTPUT_FILE_NAME,
    ).bufferedWriter()

    private fun getConvertedTypeName(
        plainObjectField: ArbrPlainObjectField,
    ): String {
        return when (plainObjectField.type) {
            ArbrPrimitiveValueType.BOOLEAN -> "Boolean"
            ArbrPrimitiveValueType.INTEGER -> "Int"
            ArbrPrimitiveValueType.LONG -> "Long"

            ArbrPrimitiveValueType.FLOAT,
            ArbrPrimitiveValueType.DOUBLE -> "Float"

            ArbrPrimitiveValueType.STRING -> "String"
        }
    }

    private fun getDirectives(
        plainObjectField: ArbrPlainObjectField,
    ): List<GraphQlSchemaFieldDirectiveModel> {
        return if (plainObjectField.required) {
            emptyList()
        } else {
            listOf(
                GraphQlSchemaFieldDirectiveModel("nullable")
            )
        }
    }

    private fun includeField(
        plainField: ArbrPlainObjectField
    ): Boolean {
        // Exclude "uuid" fields specified in gradle
        return plainField.name != RESOURCE_ID_FIELD_NAME
    }

    private fun formatDescription(description: String): String {
        return description
            .replace("\"", "\\\"")
            .trim()
    }

    private fun convert(
        plainDataTypes: List<ArbrPlainDataType>,
    ): GraphQlSchemaDocumentModel {
        val allTypes = plainDataTypes.map { plainDataType ->
            val typeTitleName = StringUtils.getCaseSuite(plainDataType.name).titleCase

            val parentReferenceFields = listOfNotNull(
                plainDataType.parentReference?.let { parentRef ->
                    GraphQlSchemaFieldModel(
                        parentRef.name,
                        formatDescription(parentRef.description),
                        StringUtils.getCaseSuite(parentRef.targetDataTypeName).titleCase,
                        directives = emptyList(),
                    )
                }
            )

            val simpleFields = plainDataType.fields
                .filter(this::includeField)
                .map { plainField ->
                    GraphQlSchemaFieldModel(
                        plainField.name,
                        formatDescription(plainField.description),
                        getConvertedTypeName(plainField),
                        getDirectives(plainField)
                    )
                }

            val referenceFields = plainDataType.relations.map { ref ->
                GraphQlSchemaFieldModel(
                    ref.name,
                    formatDescription(ref.description),
                    StringUtils.getCaseSuite(ref.targetDataTypeName).titleCase,
                    directives = listOf(
                        GraphQlSchemaFieldDirectiveModel("nullable")
                    ),
                )
            }

            val typeDescription = if (plainDataType.description.isBlank()) {
                "Object model: $typeTitleName"
            } else {
                formatDescription(plainDataType.description)
            }

            GraphQlSchemaTypeModel(
                typeTitleName,
                typeDescription,
                parentReferenceFields + simpleFields + referenceFields,
            )
        }
            .let { typeModelList ->
                val typeModelByName = typeModelList.associateBy { it.name }

                typeModelList.topSortWith(
                    { t0, t1 ->
                        t0.name.compareTo(t1.name)
                    },
                ) { typeModel ->
                    typeModel
                        .fields
                        .mapNotNull {
                            typeModelByName[it.typeName]
                        }
                }
            }

        return GraphQlSchemaDocumentModel(allTypes)
    }

    private fun loadPlainDataTypes(
        schemaSources: List<SchemaSource>,
    ): List<ArbrPlainDataType> {
        return schemaSources.flatMap { schemaSource ->
            schemaSource
                .getSchemaPaths()
                .toList()
                .flatMap { sourcePath ->
                    mapper.readValue(sourcePath.toFile(), jacksonTypeRef<List<ArbrPlainDataType>>())
                }
        }
    }

    private fun renderSimplifiedModelToCompleteNodeModel(simplifiedDocumentModel: GraphQlSchemaDocumentModel) {
        val tmpDir = Files.createTempDirectory("arbr_object_model")
        val objectModelWriter = ObjectModelGraphQlSchemaWriter.defaultOutputFileInDir(tmpDir)

        // Write default header
        val headerLines = listOf(
            "directive @nullable on FIELD_DEFINITION",
            "",
            "scalar Long",
            "",
        )

        val tmpWriter = objectModelWriter
            .write(
                simplifiedDocumentModel,
                headerLines = headerLines,
            )
        tmpWriter.flush()
        tmpWriter.close()

        val handler = DefaultCompilationTaskHandler(
            listOf(
                SchemaFileCollectionSource(
                    listOf(objectModelWriter.targetFile).stream()
                )
            )
        )

        val renderingNodeProcessor = SchemaRenderingNodeProcessor(
            outputWriter
        )
        val compilerTarget = object : CompilerTarget {
            override fun nodeProcessor(): GraphQlNodeProcessor {
                return renderingNodeProcessor
            }

            override fun setBuildOutputDir(buildOutputDir: Path) {
                //
            }
        }

        handler.handleCompilationTask(compilerTarget)
    }

    override fun convertFromSources(
        schemaSources: List<SchemaSource>,
    ) {
        val plainDataTypes = loadPlainDataTypes(schemaSources)
        val simplifiedDocumentModel = convert(plainDataTypes)
        renderSimplifiedModelToCompleteNodeModel(simplifiedDocumentModel)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ArbrToGraphQlSchemaConverter::class.java)

        private const val OUTPUT_FILE_NAME = "object_model.graphqls"

        const val RESOURCE_ID_FIELD_NAME = "uuid"
    }
}
