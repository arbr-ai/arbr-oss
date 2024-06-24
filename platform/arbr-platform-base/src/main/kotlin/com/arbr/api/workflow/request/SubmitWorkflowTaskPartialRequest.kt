package com.arbr.api.workflow.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmitWorkflowTaskPartialRequest(
    val projectFullName: String,
    val workflowType: String,
)
