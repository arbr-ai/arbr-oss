package com.arbr.api.workflow.view_model

import com.arbr.api.workflow.view_model.update.ViewModelValueUpdate
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Partial representation of a WorkflowViewModel for streaming updates without sending the entire payload every time.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowPartialViewModel(

    /**
     * General + Metadata
     */

    /**
     * Workflow ID - always present
     */
    val workflowId: String,

    val workflowStartTimeMs: ViewModelValueUpdate<Long>?,

    /**
     * Display values
     */

    /**
     * Estimated progress towards workflow completion, as a value in [0, 1].
     * Defaults to 0 when the progress is unknown or the workflow is not started.
     */
    val progress: ViewModelValueUpdate<Double>?,

    /**
     * Overall status of the workflow.
     */
    val status: ViewModelValueUpdate<WorkflowViewModelStatus>?,

    /**
     * List of active tasks that might be displayed.
     */
    val activeTasks: ViewModelValueUpdate<List<WorkflowViewModelActiveTask>>?,

    /**
     * List of file data for files modified by the workflow.
     */
    val fileData: ViewModelValueUpdate<List<WorkflowViewModelFileData>>?,

    /**
     * Statistics for the workflow's progress.
     */
    val stats: ViewModelValueUpdate<List<WorkflowViewModelStatistic>>?,

    /**
     * Information about the opened pull request, if any.
     */
    val pullRequest: ViewModelValueUpdate<WorkflowViewModelPullRequestData>?,

    /**
     * List of commits produced as part of the workflow.
     */
    val commits: ViewModelValueUpdate<List<WorkflowViewModelCommitData>>?,
)
