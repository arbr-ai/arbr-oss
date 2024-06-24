package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.ConverterTarget
import com.arbr.backdraft.task.result.TaskWorkResult

fun interface ConversionTaskHandler {

    fun handleConversionTask(target: ConverterTarget): TaskWorkResult

}