package com.arbr.api.workflow.input

data class WorkflowInputProperty(
    val name: String,
    val type: WorkflowInputPropertyType,
    val label: String?,
    val placeholder: String,
)
