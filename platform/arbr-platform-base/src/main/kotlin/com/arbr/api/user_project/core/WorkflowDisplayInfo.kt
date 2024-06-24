package com.arbr.api.user_project.core

import com.arbr.api.workflow.input.WorkflowInputModel
import com.arbr.api.workflow.input.WorkflowInputModelSchema
import com.arbr.api.workflow.input.WorkflowInputProperty
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Duration

/**
 * User-facing display info for a workflow
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowDisplayInfo<T: WorkflowInputModel>(
    val id: String,
    val title: String,
    val description: String,

    @JsonIgnore
    val estimatedDuration: Duration,

    /** Whether to display as a prototype on the project page. */
    val showOnProjectPage: Boolean,

    @JsonIgnore
    val workflowInputSchema: WorkflowInputModelSchema<T>,
) {

    @JsonGetter("inputSchema")
    fun getInputSchema(): List<WorkflowInputProperty> = workflowInputSchema.properties()
}
