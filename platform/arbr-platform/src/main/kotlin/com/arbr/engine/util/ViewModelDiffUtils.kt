package com.arbr.engine.util

import com.arbr.api.workflow.view_model.WorkflowViewModelFileData
import com.arbr.api.workflow.view_model.WorkflowViewModelFileLineAnnotation
import com.arbr.content_formats.format.DiffOperationKind
import com.arbr.model_suite.predictive_models.document_diff_alignment.SimpleTextDiffAlignmentHelper
import reactor.core.publisher.Mono

object ViewModelDiffUtils {
    private val simpleTextDiffAlignmentHelper = SimpleTextDiffAlignmentHelper.default()

    fun getViewModelFileData(
        fileName: String,
        filePath: String,
        baseContent: String,
        targetContent: String,
    ): Mono<WorkflowViewModelFileData> {
        val diffOperationsMono = simpleTextDiffAlignmentHelper.alignToDiffOperations(
            targetContent,
            baseContent,
        )

        return diffOperationsMono.map { diffOperations ->
            val annotations = diffOperations.withIndex().mapNotNull { (i, operation) ->
                when (operation.kind) {
                    DiffOperationKind.NUL,
                    DiffOperationKind.NOP -> null

                    DiffOperationKind.ADD -> WorkflowViewModelFileLineAnnotation.diffAdded(i)
                    DiffOperationKind.DEL -> WorkflowViewModelFileLineAnnotation.diffDeleted(i)
                }
            }

            // We need duplicated content here with both deleted and added lines
            val fileContents = diffOperations.joinToString("\n") {
                it.lineContent
            }

            WorkflowViewModelFileData(
                fileName,
                filePath,
                fileContents,
                annotations,
            )
        }
    }
}
