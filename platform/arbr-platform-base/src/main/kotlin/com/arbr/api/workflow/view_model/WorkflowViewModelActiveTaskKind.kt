package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowViewModelActiveTaskKind(@JsonValue val serializedName: String) {
    INDEXING("indexing"),
    BUILDING("building"),
    TESTING_IN_WEB_BROWSER("testing_in_web_browser"),
    DEPLOYING_TO_SERVER("deploying_to_server"),
    MONITORING_SYSTEM("monitoring_system");
}
