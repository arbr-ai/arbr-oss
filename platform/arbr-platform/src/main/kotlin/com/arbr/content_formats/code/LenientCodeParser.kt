package com.arbr.content_formats.code

object LenientCodeParser {
    private val splitRe = Regex("```(\\S*\\s+)?")
    private val splitRe2 = Regex("`+")
    private val regexes = listOf(splitRe, splitRe2)

    /**
     * Parse code leniently, unwrapping things like backticks.
     *
     * TODO: Incorporate real language parsers, allow skipping some number of input lines.
     */
    fun parse(sourceCodeOutput: String): String {
        val splits = mutableListOf<List<String>>()
        for (re in regexes) {
            val split = re.split(sourceCodeOutput)
            splits.add(split)

            val segments = split
                .withIndex()
                .filter { it.index % 2 == 1 }
                .map { it.value.trim() }

            val longestSegment = segments.maxByOrNull { it.length }
            if (longestSegment != null && longestSegment.length * 3 >= sourceCodeOutput.length) {
                return longestSegment + "\n"
            }
        }

        // Check for a leading escaped block
        for (i in regexes.indices) {
            val split = splits[i].map { it.trim() }

            if (split.size >= 2 && split[0].isBlank() && split[1].isNotBlank()) {
                return split[1] + "\n"
            }
        }

        return sourceCodeOutput.trim() + "\n"
    }
}