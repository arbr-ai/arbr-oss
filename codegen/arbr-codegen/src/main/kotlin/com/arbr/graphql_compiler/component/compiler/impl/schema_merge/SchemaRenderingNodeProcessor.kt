package com.arbr.graphql_compiler.component.compiler.impl.schema_merge

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.graphql.lang.common.AppendableBlock
import com.arbr.graphql.lang.common.GraphQlNodeRenderer
import com.arbr.graphql.lang.node.GraphQlLanguageNodeDirective
import com.arbr.graphql.lang.node.GraphQlLanguageNodeDirectiveDefinition
import com.arbr.graphql.lang.node.GraphQlLanguageNodeListType
import com.arbr.graphql.lang.node.GraphQlLanguageNodeNonNullType
import com.arbr.graphql.lang.node.GraphQlLanguageNodeObjectTypeDefinition
import com.arbr.graphql.lang.node.GraphQlLanguageNodeScalarTypeDefinition
import com.arbr.graphql.lang.node.GraphQlLanguageNodeType
import com.arbr.graphql.lang.node.GraphQlLanguageNodeTypeName
import java.io.Writer

fun Appendable.indent(doAppend: Appendable.() -> Unit) = doAppend(
    IndentAppendableWrapper(
        this,
        indentString = IndentAppendableWrapper.INDENT4
    )
)

open class SchemaRenderingNodeProcessor(
    override val writer: Writer,
) : GraphQlNodeRenderer() {

    private fun getConvertedType(type: GraphQlLanguageNodeType): String {
        return when (type) {
            is GraphQlLanguageNodeListType -> "[${getConvertedType(type.type!!)}]"
            is GraphQlLanguageNodeNonNullType -> "${getConvertedType(type.type!!)}!"
            is GraphQlLanguageNodeTypeName -> type.name!!
            else -> throw Exception(type.toString())
        }
    }

    private fun getDirectivesSuffix(directives: List<GraphQlLanguageNodeDirective>): String {
        return directives.joinToString("") { directive ->
            // Not supporting arguments yet
            check(directive.arguments.isNullOrEmpty())

            val directiveName = directive.name!!
            " @$directiveName"
        }
    }

    private val mapper = MapperConfig().mapper

    private fun escapeQuotes(string: String): String {
        return mapper.writeValueAsString(string).drop(1).dropLast(1)
    }

    override fun renderGraphQlLanguageNodeDirectiveDefinition(node: GraphQlLanguageNodeDirectiveDefinition): AppendableBlock? {
        val directiveName = node.name!!
        check(node.inputValueDefinitions.isNullOrEmpty())

        val locations = node.directiveLocations ?: emptyList()
        val locationString = if (locations.isEmpty()) {
            ""
        } else {
            " on ${locations.joinToString(" | ") { it.name!! }}"
        }

        return {
            append("directive @")
            append(directiveName)
            appendLine(locationString)
        }
    }

    override fun renderGraphQlLanguageNodeScalarTypeDefinition(node: GraphQlLanguageNodeScalarTypeDefinition): AppendableBlock? {
        val typeName = node.name!!

        return {
            appendLine("scalar $typeName")
        }
    }

    override fun renderGraphQlLanguageNodeObjectTypeDefinition(node: GraphQlLanguageNodeObjectTypeDefinition): AppendableBlock? {
        val typeName = node.name!!
        val fields = node.fieldDefinitions!!

        return {
            appendLine()
            node.comments.forEach { comment ->
                if (!comment.content.isNullOrBlank()) {
                    append("# ")
                    appendLine(comment.content)
                }
            }
            node.description
                ?.content
                ?.takeIf { it.isNotBlank() }
                ?.let { desc ->
                    append("\"")
                    append(escapeQuotes(desc))
                    appendLine("\"")
                }
            appendLine("type $typeName {")
            indent {
                fields.forEach { field ->
                    field.comments.forEach { comment ->
                        comment.content?.let { c ->
                            append("# ")
                            appendLine(c)
                        }
                    }
                    field.description?.content?.let { desc ->
                        append("\"")
                        append(escapeQuotes(desc))
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
                            append(getDirectivesSuffix(inputDef.directives ?: emptyList()))
                        }
                        append(")")
                    }
                    append(": ")
                    append(getConvertedType(field.type!!))
                    appendLine(getDirectivesSuffix(field.directives ?: emptyList()))
                    appendLine()
                }
            }
            appendLine("}")
            appendLine()
        }
    }

}
