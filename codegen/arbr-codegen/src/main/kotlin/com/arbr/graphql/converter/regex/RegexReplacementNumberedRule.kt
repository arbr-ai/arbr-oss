package com.arbr.graphql.converter.regex

class RegexReplacementNumberedRule(
    private val regex: Regex,
    private vararg val rules: (String) -> String,
) : RegexReplacementRule {

    override fun matches(inputString: String): Boolean {
        return regex.containsMatchIn(inputString)
    }

    override fun replace(inputString: String): String? {
        val matchResult = regex.find(inputString) ?: return null
        val sb = StringBuilder(inputString)

        matchResult.groups
            .drop(1)
            .filterNotNull()
            .withIndex()
            .sortedWith { o1, o2 ->
                val ascendingComparison = o1.value.range.last.compareTo(o2.value.range.last).let { c ->
                    if (c == 0) {
                        o1.index.compareTo(o2.index)
                    } else {
                        c
                    }
                }

                -ascendingComparison
            }
            .forEach { (i, matchGroup) ->
                try {
                    sb.replace(matchGroup.range.first, matchGroup.range.last + 1, rules[i](matchGroup.value))
                } catch (e: StringIndexOutOfBoundsException) {
                    throw e
                }
            }
        return sb.toString()
    }

    override fun toString(): String {
        return "RegexReplacementNumberedRule[${regex.pattern}]"
    }
}