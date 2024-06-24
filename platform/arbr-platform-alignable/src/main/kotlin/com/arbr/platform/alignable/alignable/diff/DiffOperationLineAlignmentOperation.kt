package com.arbr.platform.alignable.alignable.diff

import com.arbr.content_formats.format.DiffOperationKind

data class DiffOperationLineAlignmentOperation(
    val kind: DiffOperationKind?,
    val stringResult: String?,
)
