package com.arbr.core_web_dev.workflow.input.model

import com.arbr.api.workflow.input.WorkflowInputModelSchema
import com.arbr.api.workflow.input.WorkflowInputProperty

object WorkflowInputProjectStarterSchema : WorkflowInputModelSchema<WorkflowInputModelProjectStarter> {
    override val modelClass: Class<WorkflowInputModelProjectStarter> = WorkflowInputModelProjectStarter::class.java

    override fun properties(): List<WorkflowInputProperty> {
        return emptyList()
    }
}