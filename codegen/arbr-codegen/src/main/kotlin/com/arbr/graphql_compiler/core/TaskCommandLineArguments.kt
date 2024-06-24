package com.arbr.graphql_compiler.core

/**
 * Literal values of command line arguments to the process, not yet interpreted.
 */
data class TaskCommandLineArguments(
    val name: String,
    val targetId: String,
    val outputDirString: String,
    val schemaSources: List<String>,
)