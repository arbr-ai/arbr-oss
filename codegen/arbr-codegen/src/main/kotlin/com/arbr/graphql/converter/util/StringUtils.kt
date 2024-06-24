package com.arbr.graphql.converter.util

internal object StringUtils {

    private fun splitUpperCase(s: String): List<String> {
        if (s.all { it.isUpperCase() } || s.all { it.isLowerCase() }) {
            return listOf(s)
        }

        val tokens = mutableListOf<String>()

        val sb = StringBuilder()
        for (c in s) {
            if (c.isUpperCase()) {
                if (sb.isNotEmpty()) {
                    tokens.add(sb.toString())
                }
                sb.clear()
            }
            sb.append(c)
        }
        if (sb.isNotEmpty()) {
            tokens.add(sb.toString())
        }
        return tokens
    }

    /**
     * Given a string in any casing, return the suite of string casings.
     * Undefined behavior for strings not completely in one of the cases in the suite.
     */
    fun getCaseSuite(s: String): StringCaseSuite {
        val tokens = s
            .split("_")
            .flatMap(this::splitUpperCase)
            .map { it.lowercase() }

        val snakeCase = tokens.joinToString("_")
        val screamingSnakeCase = tokens.joinToString("_") { it.uppercase() }
        val titleCase = tokens.joinToString("") { it.take(1).uppercase() + it.drop(1) }
        val camelCase = (tokens.take(1) + tokens.drop(1).map { it.take(1).uppercase() + it.drop(1) }).joinToString("")

        return StringCaseSuite(titleCase, camelCase, snakeCase, screamingSnakeCase)
    }

}