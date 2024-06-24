package com.arbr.engine.services.workflow.input

import com.arbr.api.user_project.core.WorkflowDisplayInfo
import com.arbr.api.workflow.input.WorkflowInputModel

fun interface WorkflowInputDetailsProvider<WI : WorkflowInputModel> {
    /**
     * Info for user-facing display of the workflow input.
     */
    fun userFacingDisplayInfo(): WorkflowDisplayInfo<WI>
}
