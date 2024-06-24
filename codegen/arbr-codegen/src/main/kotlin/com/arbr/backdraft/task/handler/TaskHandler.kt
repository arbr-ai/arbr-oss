package com.arbr.backdraft.task.handler

import com.arbr.backdraft.target.BaseTarget
import com.arbr.backdraft.task.result.TaskWorkResult

interface TaskHandler {

    fun handleBaseTargetTask(target: BaseTarget): TaskWorkResult
}
