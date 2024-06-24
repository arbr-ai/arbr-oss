package com.arbr.graphql_compiler.model.python

enum class PythonPrimitiveValueType(override val literalNonNullForm: String) : PythonValueType {
    ANY("any"), // Not really primitive
    BOOL("bool"),
    INT("int"),
    FLOAT("float"),
    STR("str");
}