package com.arbr.graphql.converter.regex

object Regexes {

    fun replaceByRules(
        text: String,
        rules: List<RegexReplacementRule>,
    ): String {
        var workingText = text
        var anyMatch = true
        while (anyMatch) {
            anyMatch = false

            for (rule in rules) {
                rule.replaceAndCheck(workingText)?.let { next ->
                    workingText = next
                    anyMatch = true
                }
                if (anyMatch) {
                    break
                }
            }
        }

        return workingText
    }
}