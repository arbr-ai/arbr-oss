package com.topdown.parsers.lang.universal.tree_parsing

class JsonLenientTreeParsingListener(
    contentLength: Long
): TreeParsingListener(
    GlobalTreeParsingTerminalRules.jsonLenientRules,
    contentLength,
)
