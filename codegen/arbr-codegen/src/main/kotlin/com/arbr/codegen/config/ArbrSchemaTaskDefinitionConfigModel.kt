package com.arbr.codegen.config

import java.io.Serializable

data class ArbrSchemaTaskDefinitionConfigModel(
    val name: String,
    val ordinal: Int,
    val sourceFiles: List<String>,
    val outputBaseDir: String,
    val target: String,
) : Serializable

