package com.arbr.graphql_compiler.component.compiler.impl.schema_merge

import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import com.arbr.graphql.lang.node.*
import com.arbr.graphql_compiler.util.topSortWith
import org.slf4j.LoggerFactory
import java.io.Writer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter

@Suppress("UNUSED_PARAMETER")
class SchemaMergeNodeProcessor(
    outputDirPath: Path,
) : GraphQlNodeProcessor {
    private val outputFilePath = Paths.get(outputDirPath.toString(), "merged_schema.graphql")

    // Note implicitly only supports files right now
    override var writer: Writer = outputFilePath.bufferedWriter()

    init {
        val nullableDirectiveHeader = "directive @nullable on FIELD_DEFINITION"
        writer.apply {
            appendLine(nullableDirectiveHeader)
            appendLine()
        }.flush()
    }

    private val mutationFields = mutableListOf<GraphQlLanguageNodeFieldDefinition>()
    private val queryFields = mutableListOf<GraphQlLanguageNodeFieldDefinition>()
    private val finalTypeDefs = mutableListOf<GraphQlLanguageNodeObjectTypeDefinition>()

    private var currentTypeDef: GraphQlLanguageNodeObjectTypeDefinition? = null
    // private var currentScalarTypeDef: GraphQlLanguageNodeScalarTypeDefinition? = null

    private var currentTypeFields: MutableList<GraphQlLanguageNodeFieldDefinition> = mutableListOf()
    private var currentFieldType: String? = null

    private fun endTypeDef(traversalState: GraphQlLanguageNodeTraversalState) {
        val typeDef = currentTypeDef!!
        val name = typeDef.name!!
        when (name) {
            "Mutation" -> {
                mutationFields.addAll(typeDef.fieldDefinitions ?: emptyList())
            }

            "Query" -> {
                queryFields.addAll(typeDef.fieldDefinitions ?: emptyList())
            }

            else -> {
                finalTypeDefs.add(typeDef)
            }
        }
        logger.info(typeDef.toString())
        currentTypeDef = null
        currentTypeFields.clear()
    }

    private fun beginFieldDef(traversalState: GraphQlLanguageNodeTraversalState) {
        //
    }

    private fun endFieldDef(node: GraphQlLanguageNodeFieldDefinition) {
        currentTypeFields.add(node)
        currentFieldType = null
    }

    override fun processNode(traversalState: GraphQlLanguageNodeTraversalState) {
        val isEnter = when (traversalState.context.phase) {
            GraphQlLanguageNodeTraversalContextPhase.ENTER -> true
            GraphQlLanguageNodeTraversalContextPhase.LEAVE -> false
            GraphQlLanguageNodeTraversalContextPhase.BACKREF -> return // ignore
        }

        val node = traversalState.node
        when (node) {
            is GraphQlLanguageNodeFieldDefinition -> {
                if (isEnter) {
                    beginFieldDef(traversalState)
                } else {
                    endFieldDef(node)
                }
            }

            is GraphQlLanguageNodeTypeName -> {

            }

            is GraphQlLanguageNodeListType -> {

            }

            is GraphQlLanguageNodeNonNullType -> {

            }

            is GraphQlLanguageNodeDirective -> {
                //
            }

            // Top-level type definitions
            is GraphQlLanguageNodeObjectTypeDefinition -> {
                if (isEnter) {
                    currentTypeDef = node
                } else {
                    endTypeDef(traversalState)
                }
            }

            is GraphQlLanguageNodeInterfaceTypeDefinition -> {
//                if (isEnter) {
//                    beginTypeDef(traversalState)
//                } else {
//                    endTypeDef(traversalState)
//                }
            }

            is GraphQlLanguageNodeUnionTypeDefinition -> {
//                if (isEnter) {
//                    beginTypeDef(traversalState)
//                } else {
//                    endTypeDef(traversalState)
//                }
            }

            is GraphQlLanguageNodeEnumTypeDefinition -> {
//                if (isEnter) {
//                    beginTypeDef(traversalState)
//                } else {
//                    endTypeDef(traversalState)
//                }
            }

            is GraphQlLanguageNodeInputObjectTypeDefinition -> {
//                if (isEnter) {
//                    beginTypeDef(traversalState)
//                } else {
//                    endTypeDef(traversalState)
//                }
            }

            is GraphQlLanguageNodeScalarTypeDefinition -> {
                throw Exception()
//                if (isEnter) {
//                    beginTypeDef(traversalState)
//                } else {
//                    endTypeDef(traversalState)
//                }
            }

            // Operation types
            is GraphQlLanguageNodeSelectionSet -> {
                throw IllegalStateException("Unexpected selection set")
            }

            is GraphQlLanguageNodeOperationTypeDefinition -> {
                //
            }

            is GraphQlLanguageNodeDocument -> {
                if (!isEnter) {
                    writeOut()
                }
            }

            is GraphQlLanguageNodeArrayValue,
            is GraphQlLanguageNodeBooleanValue,
            is GraphQlLanguageNodeEnumValue,
            is GraphQlLanguageNodeFloatValue,
            is GraphQlLanguageNodeIntValue,
            is GraphQlLanguageNodeNullValue,
            is GraphQlLanguageNodeObjectValue,
            is GraphQlLanguageNodeScalarValue,
            is GraphQlLanguageNodeStringValue,

            is GraphQlLanguageNodeAbstractDescribedNode,
            is GraphQlLanguageNodeAbstractNode,
            is GraphQlLanguageNodeArgument,
            is GraphQlLanguageNodeComment,
            is GraphQlLanguageNodeDefinition,
            is GraphQlLanguageNodeDescribedNode,
            is GraphQlLanguageNodeDescription,
            is GraphQlLanguageNodeDirectiveDefinition,
            is GraphQlLanguageNodeDirectiveLocation,
            is GraphQlLanguageNodeDirectivesContainer -> {

            }

            is GraphQlLanguageNodeEnumTypeExtensionDefinition,
            is GraphQlLanguageNodeEnumValueDefinition,
            is GraphQlLanguageNodeField,

            is GraphQlLanguageNodeFragmentDefinition,
            is GraphQlLanguageNodeFragmentSpread,
            is GraphQlLanguageNodeImplementingTypeDefinition,
            is GraphQlLanguageNodeInlineFragment,
            is GraphQlLanguageNodeInputObjectTypeExtensionDefinition,
            is GraphQlLanguageNodeInputValueDefinition,

            is GraphQlLanguageNodeInterfaceTypeExtensionDefinition,

            is GraphQlLanguageNodeNamedNode,
            is GraphQlLanguageNodeNode,


            is GraphQlLanguageNodeObjectField,
            is GraphQlLanguageNodeObjectTypeExtensionDefinition,

            is GraphQlLanguageNodeOperationDefinition,
            is GraphQlLanguageNodeSDLDefinition,
            is GraphQlLanguageNodeSDLNamedDefinition,
            is GraphQlLanguageNodeScalarTypeExtensionDefinition,

            is GraphQlLanguageNodeSchemaDefinition,
            is GraphQlLanguageNodeSchemaExtensionDefinition,
            is GraphQlLanguageNodeSelection,
            is GraphQlLanguageNodeSelectionSetContainer,
            is GraphQlLanguageNodeSourceLocation,

            is GraphQlLanguageNodeType,
            is GraphQlLanguageNodeTypeDefinition,
            is GraphQlLanguageNodeUnionTypeExtensionDefinition,
            is GraphQlLanguageNodeValue,
            is GraphQlLanguageNodeVariableDefinition,
            is GraphQlLanguageNodeVariableReference -> {
                // Do nothing
            }
        }
    }

    private fun indent(appendable: Appendable): Appendable {
        return IndentAppendableWrapper(appendable, indentString = INDENT)
    }

    private fun typeNames(type: GraphQlLanguageNodeType): List<String> {
        return when (type) {
            is GraphQlLanguageNodeListType -> typeNames(type.type!!)
            is GraphQlLanguageNodeNonNullType -> typeNames(type.type!!)
            is GraphQlLanguageNodeTypeName -> listOf(type.name!!)
            else -> throw IllegalStateException()
        }
    }

    private fun writeOut() {
        val tdWithIndex = finalTypeDefs.withIndex().toList()
        val typeRefMap = tdWithIndex.associateBy { it.value.name!! }
        val sortedTypeDefsWithIndex = tdWithIndex.topSortWith(
            compareBy { it.index },
        ) { (_, objectTypeDef) ->
            objectTypeDef.fieldDefinitions?.mapNotNull {
                typeNames(it.type!!).firstNotNullOfOrNull { tn ->
                    typeRefMap[tn]
                }
            } ?: emptyList()
        }
        val sortedTypeDefs = sortedTypeDefsWithIndex.map { it.value }

        writer.apply {
            sortedTypeDefs.forEach { typeDef ->
                appendLine("type ${typeDef.name!!} {")
                indent(this).apply {
                    typeDef.fieldDefinitions!!.forEach { field ->
                        writeField(this, field)
                    }
                }
                appendLine("}")
                appendLine()
            }
            appendLine()
            appendLine()
            appendLine("type Mutation {")
            indent(this).apply {
                mutationFields.forEach { field ->
                    writeField(this, field)
                }
            }
            appendLine("}")
            appendLine()
            appendLine()
            appendLine("type Query {")
            indent(this).apply {
                queryFields.forEach { field ->
                    writeField(this, field)
                }
            }
            appendLine("}")
            appendLine()
        }.flush()
    }

    private fun getConvertedType(type: GraphQlLanguageNodeType): String {
        return when (type) {
            is GraphQlLanguageNodeListType -> "[${getConvertedType(type.type!!)}]"
            is GraphQlLanguageNodeNonNullType -> "${getConvertedType(type.type!!)}!"
            is GraphQlLanguageNodeTypeName -> type.name!!
            else -> throw Exception(type.toString())
        }
    }

    private fun writeField(appendable: Appendable, field: GraphQlLanguageNodeFieldDefinition) = appendable.apply {
        field.comments.forEach { comment ->
            comment.content?.let { c ->
                append("# ")
                appendLine(c)
            }
        }
        field.description?.content?.let { desc ->
            append("\"")
            append(desc)
            appendLine("\"")
        }
        append(field.name!!)

        val inputValueDefinitions = field.inputValueDefinitions
            ?: emptyList()
        if (inputValueDefinitions.isNotEmpty()) {
            append("(")
            inputValueDefinitions.forEachIndexed { i, inputDef ->
                if (i > 0) {
                    append(", ")
                }
                append(inputDef.name!!)
                append(": ")
                append(getConvertedType(inputDef.type!!))
            }
            append(")")
        }
        append(": ")
        appendLine(getConvertedType(field.type!!))
        appendLine()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaMergeNodeProcessor::class.java)
        private const val INDENT = "    "
    }
}
