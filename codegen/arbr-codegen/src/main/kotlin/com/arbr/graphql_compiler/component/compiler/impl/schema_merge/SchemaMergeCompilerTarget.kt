package com.arbr.graphql_compiler.component.compiler.impl.schema_merge

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import java.nio.file.Path
import java.nio.file.Paths

class SchemaMergeCompilerTarget : CompilerTarget {

    private var buildOutputPath: Path? = null

    override fun setBuildOutputDir(buildOutputDir: Path) {
        this.buildOutputPath = buildOutputDir
    }

    override fun nodeProcessor(): GraphQlNodeProcessor {
        val path = buildOutputPath ?: throw IllegalStateException("AugmentedSchemaCompilerTarget output dir not configured")
        val outputFilePath = Paths.get(path.toString(), "rendered_schema.graphql")
//        return SchemaMergeNodeProcessor(path)
        return SchemaRenderingNodeProcessor(
            outputFilePath.toFile().bufferedWriter()
        )
    }

}
