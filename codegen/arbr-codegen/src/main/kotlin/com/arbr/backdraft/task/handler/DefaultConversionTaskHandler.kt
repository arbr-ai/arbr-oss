package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.ConverterRuntime
import com.arbr.backdraft.target.ConverterTarget
import com.arbr.backdraft.task.result.TaskWorkResult
import com.arbr.graphql_compiler.core.SchemaSource

class DefaultConversionTaskHandler(
    private val schemaSources: List<SchemaSource>,
) : ConversionTaskHandler {
    override fun handleConversionTask(target: ConverterTarget): TaskWorkResult {
        val runtime = ConverterRuntime(target)
        runtime.convertAgainstTarget(schemaSources)

        println(runtime.getOutputDocument())

        return runtime.getWorkResult()
    }
}
