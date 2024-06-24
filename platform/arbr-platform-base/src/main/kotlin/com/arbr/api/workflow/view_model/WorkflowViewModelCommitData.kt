package com.arbr.api.workflow.view_model

import com.arbr.api.workflow.event.event_data.common.DiffStat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model of a commit event for consumption via the frontend-facing API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelCommitData(
    val commitHash: String,
    val commitMessage: String,

    /**
     * URL representing the link to the commit page on GitHub.
     */
    val url: String,

    /**
     * A summary of the change, if it exists.
     */
    val diffSummary: String?,

    /**
     * Breakdown of commit changes into files and their change stats, if it exists.
     */
    val diffStat: DiffStat?,
)