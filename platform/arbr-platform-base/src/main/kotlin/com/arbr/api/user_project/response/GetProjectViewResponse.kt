package com.arbr.api.user_project.response

import com.arbr.api.user_project.core.WorkflowDisplayInfo
import com.arbr.api.user_project.core.WorkflowInstanceDisplayInfo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GetProjectViewResponse(
    val projectFullName: String,

    val workflowInstances: List<WorkflowInstanceDisplayInfo>,

    /**
     * Info for workflows which may be initiated on the project.
     */
    val workflowPrototypes: List<WorkflowDisplayInfo<*>>,
)

