package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.core.WorkflowStatus
import com.arbr.api.workflow.core.WorkflowType
import reactor.core.publisher.Mono

interface WorkflowExecutorService {
    fun handleSubmittedWorkflowTask(
        userId: Long,
        projectFullName: String,
        workflowType: WorkflowType,
        workflowId: Long,
        projectId: Long,
        requestCreationTimestampMs: Long,
        workflowParams: Map<String, Any>,
    ): Mono<Void>

    fun getWorkflowStatus(workflowId: Long): Mono<WorkflowStatus>

    companion object {
        const val WORKFLOW_ID_CONTEXT_KEY = "com.topdown.workflow_handle_id"
    }
}

