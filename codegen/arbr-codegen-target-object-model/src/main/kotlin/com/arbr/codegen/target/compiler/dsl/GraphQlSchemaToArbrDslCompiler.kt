package com.arbr.codegen.target.compiler.dsl

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import java.nio.file.Path

class GraphQlSchemaToArbrDslCompiler: CompilerTarget {

    private var buildOutputPath: Path? = null
    private var processor: ArbrObjectModelNodeProcessor? = null

    override fun setBuildOutputDir(buildOutputDir: Path) {
        this.buildOutputPath = buildOutputDir
    }

    override fun nodeProcessor(): GraphQlNodeProcessor {
        if (processor != null) {
            throw IllegalStateException()
        }

        return ArbrObjectModelNodeProcessor()
            .also { processor = it }
    }

    fun getDataTypes(): List<ArbrPlainDataType> {
        return processor!!.getDataTypes()
    }
}