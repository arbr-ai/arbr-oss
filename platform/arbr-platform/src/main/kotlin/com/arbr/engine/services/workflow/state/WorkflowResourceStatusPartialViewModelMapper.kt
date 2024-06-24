package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.resource.WorkflowResourceType
import com.arbr.api.workflow.view_model.WorkflowPartialViewModel
import reactor.core.publisher.Mono

interface WorkflowResourceStatusPartialViewModelMapper {

    /**
     * Associated resource type for which this mapper is applicable
     */
    val resourceType: WorkflowResourceType

    /**
     * Map the given resource data to a partial view model reflecting an update.
     * TODO: Generalize view model type beyond web dev case
     */
    fun mapResourceData(
        workflowId: Long,
        resourceType: WorkflowResourceType,
        resourceData: Map<String, Any?>,
    ): Mono<WorkflowPartialViewModel>

}