package com.arbr.graphql_compiler.component.compiler.impl.schema_augmented

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import java.nio.file.Path

class AugmentedSchemaCompilerTarget : CompilerTarget {

    private var buildOutputPath: Path? = null

    override fun setBuildOutputDir(buildOutputDir: Path) {
        this.buildOutputPath = buildOutputDir
    }

    override fun nodeProcessor(): GraphQlNodeProcessor {
        val path = buildOutputPath ?: throw IllegalStateException("AugmentedSchemaCompilerTarget output dir not configured")
        return AugmentedSchemaNodeProcessor(path)
    }

}
