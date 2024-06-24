package com.arbr.codegen.target.compiler.om

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import java.nio.file.Path

/**
 * GraphQl Schema of Object Model -> Kotlin Source
 */
class GraphQlSchemaToObjectModelCompiler : CompilerTarget {

    private var buildOutputPath: Path? = null
    private var processor: GraphQlSchemaToObjectModelNodeProcessor? = null

    override fun setBuildOutputDir(buildOutputDir: Path) {
        this.buildOutputPath = buildOutputDir
    }

    override fun nodeProcessor(): GraphQlNodeProcessor {
        if (processor != null) {
            throw IllegalStateException()
        }

        val path = buildOutputPath ?: throw IllegalStateException(
            "GraphQlSchemaToObjectModelCompiler output dir not configured"
        )
        return GraphQlSchemaToObjectModelNodeProcessor(path)
            .also { processor = it }
    }
}
