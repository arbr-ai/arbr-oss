package com.arbr.core_web_dev.workflow.input.model

import com.arbr.api.workflow.input.WorkflowInputModelSchema
import com.arbr.api.workflow.input.WorkflowInputProperty

object WorkflowInputBaseSchema : WorkflowInputModelSchema<WorkflowInputModelBase> {
    override val modelClass: Class<WorkflowInputModelBase> = WorkflowInputModelBase::class.java

    override fun properties(): List<WorkflowInputProperty> {
        return emptyList()
    }
}
