package com.topdown.parsers.util

class DocumentLocusHelper(
    contents: String,
) {
    private val indexedLines = run {
        val numbered = contents.withIndex()
        val lines = mutableListOf<List<IndexedValue<Char>>>(
            emptyList() // Empty first line for zero indexing
        )
        var line = mutableListOf<IndexedValue<Char>>()
        for (indexedChar in numbered) {
            if (indexedChar.value == '\n') {
                lines.add(line)
                line = mutableListOf()
            } else {
                line.add(indexedChar)
            }
        }
        lines.add(line)

        lines.toList()
    }

    fun computeLocus(
        line: Int,
        charPositionInLine: Int,
    ): Int? {
        return if (line in indexedLines.indices) {
            val indexedLine = indexedLines[line]
            if (charPositionInLine in indexedLine.indices) {
                indexedLine[charPositionInLine].index
            } else {
                null
            }
        } else {
            null
        }
    }
}