package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelFileData(
    /**
     * Short file name, such as "Page.jsx"
     */
    val fileName: String,

    /**
     * File path within repo, such as "src/components/Page.jsx"
     */
    val filePath: String,

    /**
     * Complete contents of file
     */
    val fileContents: String,

    /**
     * List of annotations such as diff markers
     */
    val annotations: List<WorkflowViewModelFileLineAnnotation>,
)
