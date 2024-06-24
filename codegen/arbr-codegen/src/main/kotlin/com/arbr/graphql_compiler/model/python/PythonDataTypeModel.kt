package com.arbr.graphql_compiler.model.python

data class PythonDataTypeModel(
    val name: String,
    val fields: MutableList<PythonFieldModel>,
)
