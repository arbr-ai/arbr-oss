package com.arbr.engine.services.workflow.state

import com.arbr.api.workflow.view_model.WorkflowViewModelActiveTask
import com.arbr.api.workflow.view_model.WorkflowViewModelStatus
import com.arbr.platform.object_graph.artifact.WorkflowProcessorStatus
import com.arbr.platform.object_graph.core.WorkflowResourceUnaryFunction

/**
 * Was this ever actually used?
 */
interface WorkflowProcessorStatusPartialViewModelMapper {

    /**
     * Associated resource type for which this mapper is applicable
     */
    val processorFunction: WorkflowResourceUnaryFunction<*, *, *, *, *, *, *>

    /**
     * Map the given resource data to a partial view model reflecting an update.
     * TODO: Generalize view model type beyond web dev case
     */
    fun mapProcessorStatusUpdate(
        workflowId: Long,
        processorStatus: WorkflowProcessorStatus,
        processorName: String,
        resourceUuid: String,
        workflowStatus: WorkflowViewModelStatus?,
    ): WorkflowViewModelActiveTask?

}
