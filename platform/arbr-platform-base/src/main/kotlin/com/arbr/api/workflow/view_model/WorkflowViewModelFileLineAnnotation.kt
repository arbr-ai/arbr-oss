package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelFileLineAnnotation(
    /**
     * Zero-indexed line index.
     */
    val lineIndex: Int,

    /**
     * The kind of annotation for the line.
     */
    val annotationKind: WorkflowViewModelFileLineAnnotationKind,

    /**
     * Data payload associated with the annotation, such as a tooltip, if relevant.
     */
    val annotationData: Map<String, Any>,
) {

    companion object {
        fun diffAdded(lineIndex: Int): WorkflowViewModelFileLineAnnotation {
            return WorkflowViewModelFileLineAnnotation(
                lineIndex,
                WorkflowViewModelFileLineAnnotationKind.DIFF_ADDED,
                emptyMap(),
            )
        }

        fun diffDeleted(lineIndex: Int): WorkflowViewModelFileLineAnnotation {
            return WorkflowViewModelFileLineAnnotation(
                lineIndex,
                WorkflowViewModelFileLineAnnotationKind.DIFF_DELETED,
                emptyMap(),
            )
        }

        fun diffModified(lineIndex: Int): WorkflowViewModelFileLineAnnotation {
            return WorkflowViewModelFileLineAnnotation(
                lineIndex,
                WorkflowViewModelFileLineAnnotationKind.DIFF_MODIFIED,
                emptyMap(), // TODO: Consider providing information for highlighting modified part
            )
        }
    }
}