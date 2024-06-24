package com.arbr.model_suite.predictive_models.document_diff_alignment

import com.arbr.content_formats.format.DiffOperationKind
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.platform.alignable.alignable.diff.Chain
import com.arbr.alignable.language.Viability

class SimpleTextDiffViability : Viability<Chain<AlignableDiffOperation>, AlignableDiffOperation> {
    override val initialState: Chain<AlignableDiffOperation> = Chain(
        parent = null,
        node = null,
        hashCode = 98061431,
    )

    override fun inducibleEdits(
        fromState: Chain<AlignableDiffOperation>,
        proposedEdit: AlignableDiffOperation
    ): List<AlignableDiffOperation> {
        return when (proposedEdit.kind) {
            DiffOperationKind.NOP -> listOf(
                proposedEdit.copy(
                    diffOperation = proposedEdit.diffOperation.copy(
                        kind = DiffOperationKind.ADD
                    )
                )
            )

            DiffOperationKind.NUL,
            DiffOperationKind.ADD,
            DiffOperationKind.DEL -> emptyList()
        }
    }

    override fun deducibleEdits(
        fromState: Chain<AlignableDiffOperation>,
        proposedEdit: AlignableDiffOperation
    ): List<AlignableDiffOperation> {
        return when (proposedEdit.kind) {
            DiffOperationKind.NOP -> listOf(
                proposedEdit.copy(
                    diffOperation = proposedEdit.diffOperation.copy(
                        kind = DiffOperationKind.DEL
                    )
                )
            )

            DiffOperationKind.NUL,
            DiffOperationKind.ADD,
            DiffOperationKind.DEL -> emptyList()
        }
    }

    override fun isViableEdit(fromState: Chain<AlignableDiffOperation>, edit: AlignableDiffOperation): Boolean {
        return true
    }

    override fun stateCode(state: Chain<AlignableDiffOperation>): Int {
        return state.hashCode()
    }
}