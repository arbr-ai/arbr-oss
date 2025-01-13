package com.arbr.graphql.converter.regex

class RegexNamedReplacementRule(
    private val regex: Regex,
    private val groupNames: List<String>,
    private val substitute: (String, Map<String, MatchGroup>) -> String,
) : RegexReplacementRule {

    override fun matches(inputString: String): Boolean {
        return regex.containsMatchIn(inputString)
    }

    override fun replace(inputString: String): String? {
        val matchResult = regex.find(inputString) ?: return null
        val namedMatchGroups = matchResult.groups as MatchNamedGroupCollection
        val namedMatches = groupNames
            .mapNotNull { k -> namedMatchGroups[k]?.let { k to it } }
            .associate { (name, matchGroup) ->
                name to matchGroup
            }

        return if (namedMatches.isEmpty()) {
            null
        } else {
            substitute(inputString, namedMatches)
        }
    }
}