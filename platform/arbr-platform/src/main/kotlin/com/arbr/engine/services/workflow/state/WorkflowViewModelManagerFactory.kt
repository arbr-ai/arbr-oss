package com.arbr.engine.services.workflow.state

class WorkflowViewModelManagerFactory(
    /**
     * The list of application-specific mappers
     */
    private val workflowResourceStatusPartialViewModelMappers: List<WorkflowResourceStatusPartialViewModelMapper>,

    /**
     * The list of application-specific mappers for processor status
     */
    private val workflowProcessorStatusPartialViewModelMappers: List<WorkflowProcessorStatusPartialViewModelMapper>,
) {

    fun makeWorkflowViewModelManager(workflowId: Long): WorkflowViewModelManager {
        return WorkflowViewModelManager(
            workflowId,
            workflowResourceStatusPartialViewModelMappers,
            workflowProcessorStatusPartialViewModelMappers,
        )
    }
}