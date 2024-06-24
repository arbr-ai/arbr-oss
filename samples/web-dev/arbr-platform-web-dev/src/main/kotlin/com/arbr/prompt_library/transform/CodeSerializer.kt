package com.arbr.prompt_library.transform

import com.arbr.content_formats.code.LenientCodeParser

object CodeSerializer {

    fun serializeCode(body: String, languageIndicator: String=""): String {
        // Wrap in backticks if not already quoted
        val strippedBody = LenientCodeParser.parse(body.trim()).trim()
        return if (strippedBody.startsWith("`") && strippedBody.endsWith("`")) {
            // Note: does not inject new language indicator
            strippedBody
        } else {
            "```$languageIndicator\n$strippedBody\n```"
        }
    }
}