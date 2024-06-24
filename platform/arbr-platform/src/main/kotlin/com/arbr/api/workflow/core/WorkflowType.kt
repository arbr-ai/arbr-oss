package com.arbr.api.workflow.core

import com.fasterxml.jackson.annotation.JsonValue

enum class WorkflowType(@JsonValue val serializedName: String) {
    HELLO_WORLD("workflow-hello-world"),
    MULTI_FEATURE("workflow-multi-feature"),
    MIGRATE_REDUX("workflow-migrate-redux"),
    MULTI_FEATURE_FIGMA("workflow-multi-feature-figma"),
    STARTER_PROJECT("workflow-starter"),
    PROJECT_ANALYZE_STATIC("workflow-static"),
}
