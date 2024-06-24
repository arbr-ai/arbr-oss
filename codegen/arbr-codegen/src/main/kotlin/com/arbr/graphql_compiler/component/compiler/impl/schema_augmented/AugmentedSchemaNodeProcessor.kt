package com.arbr.graphql_compiler.component.compiler.impl.schema_augmented

import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import com.arbr.graphql.lang.common.GraphQlSimpleNodeType
import com.arbr.graphql.lang.node.*
import com.arbr.graphql_compiler.model.schema_augmented.*
import com.arbr.graphql_compiler.util.StringUtils
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.slf4j.LoggerFactory
import java.io.Writer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter

class AugmentedSchemaNodeProcessor(
    outputDirPath: Path,
) : GraphQlNodeProcessor {
    private val outputFilePath = Paths.get(outputDirPath.toString(), "object_model_augmented.graphql")

    // Note implicitly only supports files right now
    override var writer: Writer = outputFilePath.bufferedWriter()
    private val mustacheFactory = DefaultMustacheFactory()
    private val dataClassMustache: Mustache = mustacheFactory.compile(
        "src/main/resources/templates/schema_augmented/object_model_augmented.graphql.mustache"
    )

    // These should probably be stacks to be safe
    private var dataTypeUnderCreation: AugmentedSchemaTypeModel? = null
    private var dataTypeAssociatedScalarTypes = mutableListOf<AugmentedSchemaScalarTypeModel>()
    private var fieldUnderCreation: AugmentedSchemaFieldModel? = null

    init {
        val nullableDirectiveHeader = "directive @nullable on FIELD_DEFINITION"
        writer.apply {
            appendLine(nullableDirectiveHeader)
            appendLine()
        }.flush()
    }

    private fun addScalarTypeDef(
        qualifiedTypeName: String,
    ) {
        dataTypeAssociatedScalarTypes.add(
            AugmentedSchemaScalarTypeModel(
                qualifiedTypeName,
                mutableListOf(), // TODO: Primitive spec directive
            )
        )
    }

    private fun getAugmentedTypeByName(
        dataType: AugmentedSchemaTypeModel,
        field: AugmentedSchemaFieldModel,
        baseType: String,
    ): String {
        val dataTypeTitle = StringUtils.getCaseSuite(dataType.name).titleCase
        val fieldTitle = StringUtils.getCaseSuite(field.name).titleCase
        val qualifiedTypeName = "${dataTypeTitle}_$fieldTitle"

        return when (baseType) {
            "String", "StringValue" -> {
                addScalarTypeDef(qualifiedTypeName)
                qualifiedTypeName
            }
            "Int", "IntValue" -> {
                addScalarTypeDef(qualifiedTypeName)
                qualifiedTypeName
            }
            "Float", "FloatValue" -> {
                addScalarTypeDef(qualifiedTypeName)
                qualifiedTypeName
            }
            "Boolean", "BooleanValue" -> {
                addScalarTypeDef(qualifiedTypeName)
                qualifiedTypeName
            }
            "ID", "IDValue" -> {
                addScalarTypeDef(qualifiedTypeName)
                qualifiedTypeName
            }
            else -> {
                // Already a reference type
                baseType
            }
        }
    }

    private fun writeDataTypeModel(
        dataTypeModel: AugmentedSchemaTypeModel,
        scalarTypeModels: List<AugmentedSchemaScalarTypeModel>,
    ) {
        writer = dataClassMustache.execute(
            writer,
            AugmentedSchemaDocumentModel(mutableListOf(dataTypeModel), scalarTypeModels.toMutableList()),
        )
        writer.flush()
    }

    private fun enterObjectTypeDefinition(traversalState: GraphQlLanguageNodeTraversalState) {
        val objTypeDefinitionNode = traversalState.node as GraphQlLanguageNodeObjectTypeDefinition
        val name = objTypeDefinitionNode.name
        if (name == null) {
            logger.warn("Skipping type definition missing name. Node ID: ${traversalState.context.nodeId}")
            return
        }
        check(dataTypeUnderCreation == null) {
            "Unexpected data type already under creation"
        }

        val nodeDescription = objTypeDefinitionNode.description?.content ?: ""

        val definitionNameCaseSuite = StringUtils.getCaseSuite(name)
        dataTypeUnderCreation = AugmentedSchemaTypeModel(
            definitionNameCaseSuite.titleCase,
            nodeDescription,
            mutableListOf(),
        )
    }

    private fun leaveObjectTypeDefinition(traversalState: GraphQlLanguageNodeTraversalState) {
        val dataType = dataTypeUnderCreation
            ?: kotlin.run {
                logger.warn("Missing data type model on leaving. Node ID: ${traversalState.context.nodeId}")
                return
            }
        val scalarTypes = dataTypeAssociatedScalarTypes.toList()

        dataTypeUnderCreation = null
        dataTypeAssociatedScalarTypes.clear()
        writeDataTypeModel(dataType, scalarTypes)
    }

    private fun enterFieldDefinition(traversalState: GraphQlLanguageNodeTraversalState) {
        val fieldDefinitionNode = traversalState.node as GraphQlLanguageNodeFieldDefinition
        check(fieldUnderCreation == null) {
            "Unexpected field already under creation"
        }

        val name = fieldDefinitionNode.name ?: return
        val nodeDescription = fieldDefinitionNode.description?.content ?: ""

        fieldUnderCreation = AugmentedSchemaFieldModel(
            name,
            nodeDescription,
            "",
            mutableListOf(),
        )
    }

    private fun leaveFieldDefinition() {
        val newFieldModel = fieldUnderCreation!!
        fieldUnderCreation = null
        dataTypeUnderCreation!!.fields.add(newFieldModel)
    }

    override fun processNode(traversalState: GraphQlLanguageNodeTraversalState) {
        val isEnter = when (traversalState.context.phase) {
            GraphQlLanguageNodeTraversalContextPhase.ENTER -> true
            GraphQlLanguageNodeTraversalContextPhase.LEAVE -> false
            GraphQlLanguageNodeTraversalContextPhase.BACKREF -> return // ignore
        }

        when (traversalState.context.nodeType) {
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_OBJECT_TYPE_DEFINITION -> {
                if (isEnter) {
                    enterObjectTypeDefinition(traversalState)
                } else {
                    leaveObjectTypeDefinition(traversalState)
                }
            }
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_FIELD_DEFINITION -> {
                if (isEnter) {
                    enterFieldDefinition(traversalState)
                } else {
                    leaveFieldDefinition()
                }
            }
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_TYPE_NAME -> {
                if (!isEnter) {
                    val name = (traversalState.node as GraphQlLanguageNodeTypeName).name
                    if (name != null) {
                        fieldUnderCreation?.typeName = getAugmentedTypeByName(
                            dataTypeUnderCreation!!,
                            fieldUnderCreation!!,
                            name,
                        )
                    }
                }
            }
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_LIST_TYPE -> {
                if (!isEnter) {
                    fieldUnderCreation?.let { f ->
                        f.typeName = "[${f.typeName}]"
                    }
                }
            }
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_NON_NULL_TYPE -> {
                if (!isEnter) {
                    fieldUnderCreation?.let { f ->
                        f.typeName = "${f.typeName}!"
                    }
                }
            }
            GraphQlSimpleNodeType.GRAPH_QL_LANGUAGE_NODE_DIRECTIVE -> {
                if (!isEnter) {
                    fieldUnderCreation?.let { f ->
                        val name = (traversalState.node as GraphQlLanguageNodeDirective).name!!
                        f.directives.add(
                            AugmentedSchemaFieldDirectiveModel(name)
                        )
                    }
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AugmentedSchemaNodeProcessor::class.java)
    }
}
