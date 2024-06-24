package com.arbr.api.workflow.response

import com.arbr.api.user_project.core.WorkflowDisplayInfo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetWorkflowPrototypesResponse(
    /**
     * Info for workflows which may be initiated generally.
     */
    val workflowPrototypes: List<WorkflowDisplayInfo<*>>,
)