package com.arbr.og_engine.file_system

data class ShellOutput(
    val status: Int,
    val stdout: List<String>,
    val stderr: List<String>
) {

    private fun grepLines(
        lines: List<String>,
        findRegex: Regex,
        contextLines: Int,
        segmentDivider: String?,
    ): List<String> {
        val indices = lines.withIndex().mapNotNull { (i, line) ->
            findRegex.find(line)?.let {
                (i - contextLines)..(i + contextLines)
            }
        }
            .flatMap { range -> range.toList() }
            .filter { it in lines.indices }
            .toSet()
            .sorted()

        val outLines = mutableListOf<String>()
        var lastIdx: Int? = null
        for (i in indices) {
            if (segmentDivider != null && lastIdx != null && lastIdx != i - 1) {
                // Not contiguous
                outLines.add(segmentDivider)
            }
            outLines.add(lines[i])

            lastIdx = i
        }

        return outLines
    }

    fun grep(
        findRegex: Regex,
        contextLines: Int = 0,
        segmentDivider: String? = null,
    ): ShellOutput = ShellOutput(
        status,
        grepLines(stdout, findRegex, contextLines, segmentDivider),
        grepLines(stderr, findRegex, contextLines, segmentDivider),
    )
}