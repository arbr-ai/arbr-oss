package com.arbr.graphql_compiler.model.python

data class PythonObjectRefType(
    private val objectTypeName: String
): PythonValueType {
    override val literalNonNullForm: String
        get() = objectTypeName
}