package com.arbr.api.workflow.view_model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class WorkflowViewModelStatistic(
    val kind: WorkflowViewModelStatisticKind,
    val displayString: String,
    val quantity: Long,
) {

    companion object {
        const val FILES_EDITED_DISPLAY_STRING = "Files Edited"
        const val LINES_OF_CODE_TOTAL_DISPLAY_STRING = "Lines of Code"
        const val LINES_OF_CODE_ADDED_DISPLAY_STRING = "Lines of Code Added"
        const val LINES_OF_CODE_DELETED_DISPLAY_STRING = "Lines of Code Deleted"

        fun filesEdited(numFilesEdited: Long): WorkflowViewModelStatistic {
            return WorkflowViewModelStatistic(
                WorkflowViewModelStatisticKind.FILES_EDITED,
                FILES_EDITED_DISPLAY_STRING,
                numFilesEdited,
            )
        }

        fun linesOfCodeTotal(numLinesOfCodeTotal: Long): WorkflowViewModelStatistic {
            return WorkflowViewModelStatistic(
                WorkflowViewModelStatisticKind.LINES_OF_CODE_TOTAL,
                LINES_OF_CODE_TOTAL_DISPLAY_STRING,
                numLinesOfCodeTotal,
            )
        }

        fun linesOfCodeAdded(numLinesOfCodeAdded: Long): WorkflowViewModelStatistic {
            return WorkflowViewModelStatistic(
                WorkflowViewModelStatisticKind.LINES_OF_CODE_ADDED,
                LINES_OF_CODE_ADDED_DISPLAY_STRING,
                numLinesOfCodeAdded,
            )
        }

        fun linesOfCodeDeleted(numLinesOfCodeDeleted: Long): WorkflowViewModelStatistic {
            return WorkflowViewModelStatistic(
                WorkflowViewModelStatisticKind.LINES_OF_CODE_DELETED,
                LINES_OF_CODE_DELETED_DISPLAY_STRING,
                numLinesOfCodeDeleted,
            )
        }
    }
}