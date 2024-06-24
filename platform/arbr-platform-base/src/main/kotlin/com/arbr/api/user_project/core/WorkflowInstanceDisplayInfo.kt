package com.arbr.api.user_project.core

import com.arbr.api.workflow.core.WorkflowStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * User-facing display info for a workflow which has been initiated, i.e. one which has a notion of progress in a
 * project.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowInstanceDisplayInfo(
    /** ID of the workflow prototype. */
    val id: String,
    val title: String,
    val description: String,

    /** ID of the instance of the workflow. */
    val handleId: String,
    val status: WorkflowStatus,
    val progress: Double?,
)
