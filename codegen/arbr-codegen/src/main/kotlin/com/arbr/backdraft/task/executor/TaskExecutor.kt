package com.arbr.backdraft.task.executor

import com.arbr.backdraft.task.handler.TaskHandler
import com.arbr.backdraft.task.result.TaskWorkResult
import com.arbr.graphql_compiler.core.TaskCommandLineArguments

interface TaskExecutor {
    val taskHandler: TaskHandler

    fun executeSingleTask(
        commandLineArgs: TaskCommandLineArguments
    ): TaskWorkResult
}

