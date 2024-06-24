package com.arbr.core_web_dev.workflow.input.model

import com.arbr.api.workflow.input.WorkflowInputModel
import com.arbr.core_web_dev.workflow.input.base.ProjectInfoBearer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowInputModelBase(
    override val projectFullName: String,
    override val baseBranch: String?,
) : WorkflowInputModel, ProjectInfoBearer
