package com.arbr.graphql_compiler.model.python

import com.arbr.graphql.lang.node.GraphQlLanguageNodeDirective

data class PythonFieldModel(
    val name: String,
    var valueType: PythonValueType,
    val description: PythonFieldDescription? = null,

    /**
     * TODO: Support
     */
    val directives: List<GraphQlLanguageNodeDirective> = emptyList(),
)