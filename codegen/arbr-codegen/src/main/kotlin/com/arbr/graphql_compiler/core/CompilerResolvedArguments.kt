package com.arbr.graphql_compiler.core

import com.arbr.backdraft.target.CompilerTarget
import java.nio.file.Path

data class CompilerResolvedArguments(
    val compilerTarget: CompilerTarget,
    val buildOutputDir: Path,
    val schemaSources: List<SchemaSource>,
)