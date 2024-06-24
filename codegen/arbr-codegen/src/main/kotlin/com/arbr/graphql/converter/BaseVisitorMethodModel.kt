package com.arbr.graphql.converter

import java.lang.reflect.Type

data class BaseVisitorMethodModel(
    val name: String,
    val nodeParameterName: String,
    val nodeParameterType: Type,
    val tcParameterName: String,
    val tcParameterType: Type,
)
