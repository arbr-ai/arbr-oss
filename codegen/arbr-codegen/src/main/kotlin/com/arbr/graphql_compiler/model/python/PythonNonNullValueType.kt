package com.arbr.graphql_compiler.model.python

data class PythonNonNullValueType<T : PythonValueType>(
    private val innerValueType: T,
) : PythonValueType {
    override val literalNonNullForm: String = innerValueType.literalNonNullForm
    override val nullable: Boolean = false
}