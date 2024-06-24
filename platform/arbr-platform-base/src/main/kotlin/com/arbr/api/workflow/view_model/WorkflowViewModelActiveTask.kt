package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelActiveTask(
    val kind: WorkflowViewModelActiveTaskKind,
    val displayString: String,
) {
    companion object {
        const val INDEXING_DISPLAY_STRING = "Indexing Project"
        const val BUILDING_DISPLAY_STRING = "Building"
        const val TESTING_WEB_BROWSER_DISPLAY_STRING = "Testing in Web Browser"
        const val DEPLOYING_DISPLAY_STRING = "Deploying to Server"
        const val MONITORING_DISPLAY_STRING = "Monitoring System"

        fun indexing(): WorkflowViewModelActiveTask {
            return WorkflowViewModelActiveTask(
                WorkflowViewModelActiveTaskKind.INDEXING,
                INDEXING_DISPLAY_STRING,
            )
        }

        fun building(): WorkflowViewModelActiveTask {
            return WorkflowViewModelActiveTask(
                WorkflowViewModelActiveTaskKind.BUILDING,
                BUILDING_DISPLAY_STRING,
            )
        }

        fun testingInWebBrowser(): WorkflowViewModelActiveTask {
            return WorkflowViewModelActiveTask(
                WorkflowViewModelActiveTaskKind.TESTING_IN_WEB_BROWSER,
                TESTING_WEB_BROWSER_DISPLAY_STRING,
            )
        }

        fun deployingToServer(): WorkflowViewModelActiveTask {
            return WorkflowViewModelActiveTask(
                WorkflowViewModelActiveTaskKind.DEPLOYING_TO_SERVER,
                DEPLOYING_DISPLAY_STRING,
            )
        }

        fun monitoringSystem(): WorkflowViewModelActiveTask {
            return WorkflowViewModelActiveTask(
                WorkflowViewModelActiveTaskKind.MONITORING_SYSTEM,
                MONITORING_DISPLAY_STRING,
            )
        }
    }
}
