package com.arbr.core_web_dev.workflow.input.base

import com.arbr.core_web_dev.workflow.input.model.IterationTaskContainer
import com.fasterxml.jackson.annotation.JsonIgnore

interface TaskInfoBearer {
    @JsonIgnore
    fun getTaskInfo(): IterationTaskContainer
}
