package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.CompilerTarget
import com.arbr.backdraft.task.result.TaskWorkResult

fun interface CompilationTaskHandler {

    fun handleCompilationTask(target: CompilerTarget): TaskWorkResult
}
