package com.arbr.core_web_dev.workflow.input.model

import com.arbr.api.workflow.input.WorkflowInputModelSchema
import com.arbr.api.workflow.input.WorkflowInputProperty
import com.arbr.api.workflow.input.WorkflowInputPropertyType

object WorkflowInputFeatureQuerySchema : WorkflowInputModelSchema<WorkflowInputModelFeatureQuery> {
    override val modelClass: Class<WorkflowInputModelFeatureQuery> = WorkflowInputModelFeatureQuery::class.java

    override fun properties(): List<WorkflowInputProperty> {
        return listOf(
            WorkflowInputProperty(
                "task_query",
                WorkflowInputPropertyType.TEXT,
                label = null,
                placeholder = "Implement a home page...",
            ),
        )
    }
}