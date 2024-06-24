package com.arbr.graphql.converter.model

import java.lang.reflect.Type

internal data class NodeBaseField(
    val name: String,
    val accessorLiteral: String,
    val nodeType: Class<*>,
    val fieldValueType: Type,
)
