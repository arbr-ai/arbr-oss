package com.arbr.api.workflow.message

import com.arbr.api.workflow.core.WorkflowStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Message passed along the Kafka stream representing the request to start the workflow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class EndWorkflowTaskMessage(
    val userId: Long,

    /**
     * Workflow ID as recorded in the database.
     */
    val workflowId: Long,

    /**
     * The status to apply to the ended workflow task.
     */
    val workflowStatus: WorkflowStatus,
)
