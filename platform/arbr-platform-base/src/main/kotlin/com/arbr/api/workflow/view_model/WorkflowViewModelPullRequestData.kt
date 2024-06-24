package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Model of a pull request event for consumption via the frontend-facing API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelPullRequestData(
    val title: String,
    val link: String,
)