package com.arbr.engine.services.workflow.state

import com.arbr.platform.object_graph.impl.ObjectModelResource
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

interface WorkflowResourceLifecycleService {

    fun hasStrongPathToProjectOwner(
        resource: ObjectModelResource<*, *, *>,
    ): Boolean

}
