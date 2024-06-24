package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowViewModelStatisticKind(@JsonValue val serializedName: String) {
    FILES_EDITED("files_edited"),
    LINES_OF_CODE_TOTAL("lines_of_code_total"),
    LINES_OF_CODE_ADDED("lines_of_code_added"),
    LINES_OF_CODE_DELETED("lines_of_code_deleted");
}

