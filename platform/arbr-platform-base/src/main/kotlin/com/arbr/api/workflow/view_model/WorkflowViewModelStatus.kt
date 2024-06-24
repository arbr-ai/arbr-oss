package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelStatus(
    val kind: WorkflowViewModelStatusKind,
    val displayString: String,
) {
    companion object {
        const val BOOTING_DISPLAY_STRING = "Booting"
        const val PLANNING_DISPLAY_STRING = "Planning"
        const val DEVELOPING_DISPLAY_STRING = "Developing"
        const val TESTING_DISPLAY_STRING = "Testing"
        const val DEPLOYING_DISPLAY_STRING = "Deploying"
        const val FINISHING_DISPLAY_STRING = "Finishing"
        const val NOT_STARTED_DISPLAY_STRING = "Not Started"
        const val SUCCEEDED_DISPLAY_STRING = "Succeeded"
        const val CANCELLED_DISPLAY_STRING = "Cancelled"
        const val FAILED_DISPLAY_STRING = "Failed"
        const val UNKNOWN_DISPLAY_STRING = "Unknown"

        fun booting(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.BOOTING, BOOTING_DISPLAY_STRING)

        fun planning(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.PLANNING, PLANNING_DISPLAY_STRING)

        fun developing(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.DEVELOPING, DEVELOPING_DISPLAY_STRING)

        fun testing(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.TESTING, TESTING_DISPLAY_STRING)

        fun deploying(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.DEPLOYING, DEPLOYING_DISPLAY_STRING)

        fun finishing(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.FINISHING, FINISHING_DISPLAY_STRING)

        fun notStarted(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.NOT_STARTED, NOT_STARTED_DISPLAY_STRING)

        fun succeeded(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.SUCCEEDED, SUCCEEDED_DISPLAY_STRING)

        fun cancelled(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.CANCELLED, CANCELLED_DISPLAY_STRING)

        fun failed(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.FAILED, FAILED_DISPLAY_STRING)

        fun unknown(): WorkflowViewModelStatus =
            WorkflowViewModelStatus(WorkflowViewModelStatusKind.UNKNOWN, UNKNOWN_DISPLAY_STRING)
    }
}
