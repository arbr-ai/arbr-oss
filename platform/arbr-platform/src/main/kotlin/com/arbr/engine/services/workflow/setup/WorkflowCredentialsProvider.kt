package com.arbr.engine.services.workflow.setup

import reactor.core.publisher.Mono

interface WorkflowCredentialsProvider {
    fun getUserWorkflowCredentials(
        userId: Long,
        workflowId: Long,
    ): Mono<Map<String, Any>>
}