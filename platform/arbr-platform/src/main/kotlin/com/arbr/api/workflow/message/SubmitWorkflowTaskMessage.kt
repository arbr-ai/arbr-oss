package com.arbr.api.workflow.message

import com.arbr.api.workflow.core.WorkflowType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Message passed along the Kafka stream representing the request to start the workflow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmitWorkflowTaskMessage(
    val userId: Long,
    val projectFullName: String,
    val workflowType: WorkflowType,
    val requestCreationTimestampMs: Long,

    /**
     * Workflow ID as recorded in the database.
     */
    val workflowId: Long,

    /**
     * Project ID as recorded in the database.
     */
    val projectId: Long,

    /**
     * Inner parameters for the workflow.
     */
    val workflowParams: Map<String, Any>,
)
