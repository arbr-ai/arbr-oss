package com.arbr.platform.object_graph.generics

import reactor.core.publisher.Mono

/**
 * Central shutoff for workflows
 * Many disparate implementations, needs to be unified
 */
interface WorkflowCancellationService {

    fun workflowIsCancelled(
        workflowId: Long
    ): Mono<Boolean>

    fun cancelWorkflow(
        workflowId: Long
    ): Mono<Void>

}

