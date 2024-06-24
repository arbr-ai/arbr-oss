package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Workflow view model representation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModel(

    /**
     * General + Metadata
     */

    val workflowId: String,
    val workflowStartTimeMs: Long,

    /**
     * Display values
     */

    /**
     * Estimated progress towards workflow completion, as a value in [0, 1].
     * Defaults to 0 when the progress is unknown or the workflow is not started.
     */
    val progress: Double,

    /**
     * Overall status of the workflow.
     */
    val status: WorkflowViewModelStatus,

    /**
     * List of active tasks that might be displayed.
     */
    val activeTasks: List<WorkflowViewModelActiveTask>,

    /**
     * List of file data for files modified by the workflow.
     */
    val fileData: List<WorkflowViewModelFileData>,

    /**
     * Statistics for the workflow's progress.
     */
    val stats: List<WorkflowViewModelStatistic>,

    /**
     * Information about the opened pull request, if any.
     */
    val pullRequest: WorkflowViewModelPullRequestData?,

    /**
     * List of commits produced as part of the workflow.
     */
    val commits: List<WorkflowViewModelCommitData>,
)
