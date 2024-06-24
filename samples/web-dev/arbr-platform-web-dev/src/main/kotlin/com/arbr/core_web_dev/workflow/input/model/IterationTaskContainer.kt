package com.arbr.core_web_dev.workflow.input.model

import com.arbr.core_web_dev.workflow.input.base.TaskInfoBearer
import com.arbr.object_model.core.resource.field.ArbrProjectDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectFullNameValue
import com.arbr.object_model.core.resource.field.ArbrProjectPlatformValue
import com.arbr.object_model.core.resource.field.ArbrTaskBranchNameValue
import com.arbr.object_model.core.resource.field.ArbrTaskTaskQueryValue

data class IterationTaskContainer(
    val projectName: ArbrProjectFullNameValue,
    val platform: ArbrProjectPlatformValue,
    val projectDescription: ArbrProjectDescriptionValue,
    val taskQuery: ArbrTaskTaskQueryValue,
    val baseBranch: ArbrTaskBranchNameValue?,
) : TaskInfoBearer {
    override fun getTaskInfo(): IterationTaskContainer {
        return this
    }
}