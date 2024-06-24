package com.arbr.platform.object_graph.core

import com.arbr.platform.object_graph.impl.ObjectModelResource

/**
 * Root resources for a workflow (project task) being executed in the context of a project.
 */
data class WorkflowResourceModel(
    val workflowHandleId: String,
    val root: ObjectModelResource<*, *, *>,
)
