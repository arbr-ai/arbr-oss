package com.arbr.graphql.converter.regex

import org.slf4j.LoggerFactory

interface RegexReplacementRule {
    fun matches(inputString: String): Boolean

    fun replace(inputString: String): String?

    fun replaceAndCheck(inputString: String): String? {
        val result = replace(inputString) ?: return null

        // Check an obvious infinite loop case. Better check would be state memoization
        val replicated = inputString in result && matches(result)
        check(!replicated) {
            "Regex replacement rule $this replicated input: $inputString -> $result"
        }

        logger.debug("$this : $inputString -> $result")
        return result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RegexReplacementRule::class.java)
    }
}