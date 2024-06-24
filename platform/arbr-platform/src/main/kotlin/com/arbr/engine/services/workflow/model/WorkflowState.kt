package com.arbr.engine.services.workflow.model

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.db.public.tables.pojos.UserProjectWorkflow
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.slf4j.LoggerFactory

/**
 * A workflow state to be emitted by the stream for display in the frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WorkflowState(
    val workflowId: String,
    val workflowType: String,
    val createdTimestamp: Long,
    val status: WorkflowStatus,
    val statusRecordedTimestamp: Long,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowState::class.java)

        fun fromUserProjectWorkflow(
            userProjectWorkflow: UserProjectWorkflow,
        ): WorkflowState? {
            return try {
                WorkflowState(
                    userProjectWorkflow.id!!.toString(),
                    userProjectWorkflow.workflowType,
                    userProjectWorkflow.creationTimestamp,
                    WorkflowStatus.values()[userProjectWorkflow.lastStatus],
                    userProjectWorkflow.lastStatusRecordedTimestamp,
                )
            } catch (e: Exception) {
                logger.warn("Failed to parse user project workflow", e)
                null
            }
        }
    }
}
