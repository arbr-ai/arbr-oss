package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.BaseTarget
import com.arbr.backdraft.target.CompilerTarget
import com.arbr.backdraft.target.ConverterTarget
import com.arbr.backdraft.task.result.TaskWorkResult

interface DelegatingTaskHandler: TaskHandler {
    val compilationTaskHandler: CompilationTaskHandler
    val conversionTaskHandler: ConversionTaskHandler

    override fun handleBaseTargetTask(target: BaseTarget): TaskWorkResult {
        return when (target) {
            is CompilerTarget -> compilationTaskHandler.handleCompilationTask(target)
            is ConverterTarget -> conversionTaskHandler.handleConversionTask(target)
        }
    }
}