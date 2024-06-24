package com.arbr.content_formats.format

data class DiffOperation(
    val kind: DiffOperationKind,
    val lineContent: String,
    val lineNumber: Int, // Exists for identification but does not align. Zero-indexed!
) {

    override fun toString(): String {
        val paddedLineContent = lineContent.padEnd(48)

        return "diff ${kind.name}@${lineNumber}[${paddedLineContent}]"
    }
}
