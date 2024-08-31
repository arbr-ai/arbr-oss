package com.arbr.api.workflow.input

interface WorkflowInputModelSchema<T: WorkflowInputModel> {
    val modelClass: Class<T>

    fun properties(): List<WorkflowInputProperty>
}
