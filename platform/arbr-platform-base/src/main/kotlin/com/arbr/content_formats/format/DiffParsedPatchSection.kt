package com.arbr.content_formats.format

import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.fasterxml.jackson.annotation.JsonIgnore

data class DiffParsedPatchSection(
    val lineStart: Int?,
    val lineEnd: Int?,
    val targetLineStart: Int?,
    val targetLineEnd: Int?,
    val operations: LinearOrderList<DiffOperation>,
) {
    @JsonIgnore
    fun inverted(): DiffParsedPatchSection {
        return DiffParsedPatchSection(
            targetLineStart,
            targetLineEnd,
            lineStart,
            lineEnd,
            operations.map { operation ->
                when (operation.kind) {
                    DiffOperationKind.NUL,
                    DiffOperationKind.NOP -> operation

                    DiffOperationKind.ADD -> DiffOperation(
                        DiffOperationKind.DEL,
                        operation.lineContent,
                        operation.lineNumber,
                    )

                    DiffOperationKind.DEL -> DiffOperation(
                        DiffOperationKind.ADD,
                        operation.lineContent,
                        operation.lineNumber,
                    )
                }
            }.asLinearOrderList()
        )
    }
}