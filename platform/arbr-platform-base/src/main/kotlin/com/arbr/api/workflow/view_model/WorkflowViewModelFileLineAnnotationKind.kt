package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowViewModelFileLineAnnotationKind(@JsonValue val serializedName: String) {
    /**
     * The line was added as part of the diff.
     */
    DIFF_ADDED("diff_added"),

    /**
     * The line was deleted as part of the diff.
     */
    DIFF_DELETED("diff_deleted"),

    /**
     * The line was modified as part of the diff.
     */
    DIFF_MODIFIED("diff_modified"),
}