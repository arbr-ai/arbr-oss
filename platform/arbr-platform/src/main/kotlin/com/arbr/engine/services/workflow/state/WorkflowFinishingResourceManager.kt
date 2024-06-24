package com.arbr.engine.services.workflow.state

import reactor.core.publisher.Mono

interface WorkflowFinishingResourceManager {
    fun cancelWorkflow(
        workflowHandleId: String,
    ): Mono<Void>

    fun finishWorkflow(
        workflowHandleId: String,
    ): Mono<Void>
}