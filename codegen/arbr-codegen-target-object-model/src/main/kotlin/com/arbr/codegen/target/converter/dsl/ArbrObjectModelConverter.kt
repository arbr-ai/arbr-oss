package com.arbr.codegen.target.converter.dsl

import com.arbr.backdraft.target.ConverterProcess
import com.arbr.backdraft.target.ConverterTarget
import com.arbr.backdraft.target.GraphQlConverter
import java.nio.file.Path

class ArbrObjectModelConverter : ConverterTarget {
    private var buildOutputPath: Path? = null

    override fun getBuildOutputDir(): Path? {
        return buildOutputPath
    }

    override fun setBuildOutputDir(buildOutputDir: Path) {
        this.buildOutputPath = buildOutputDir
    }

    private fun makeConverter(): GraphQlConverter {
        val path = buildOutputPath ?: throw IllegalStateException()
        return ArbrToGraphQlSchemaConverter(
            path
        )
    }

    override fun initialize(): ConverterProcess {
        return ConverterProcess(
            makeConverter(),
        )
    }
}
