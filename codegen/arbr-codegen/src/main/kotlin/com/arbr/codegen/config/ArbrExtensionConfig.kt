package com.arbr.codegen.config

import java.io.Serializable

data class ArbrExtensionConfig(
    val inputBaseDir: String,
    val outputBaseDir: String,
    val definitions: List<ArbrSchemaTaskDefinitionConfigModel>,
) : Serializable