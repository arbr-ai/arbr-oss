package com.arbr.graphql_compiler.model.python

import com.arbr.graphql.lang.node.GraphQlLanguageNodeDescription

data class PythonFieldDescription(
    val content: String,
    val isMultiLine: Boolean,
): RenderableExpression {

    override fun render(): String {
        return if (isMultiLine) {
            val interiorLines = content.split("\n").joinToString("\n") { " * ${it.trim()}" }
            "/**\n$interiorLines\n */"
        } else {
            "// ${content.trim()}"
        }
    }

    companion object {
        fun of(description: GraphQlLanguageNodeDescription?): PythonFieldDescription? {
            val content = description?.content ?: return null

            return PythonFieldDescription(
                content,
                description.multiLine,
            )
        }
    }
}