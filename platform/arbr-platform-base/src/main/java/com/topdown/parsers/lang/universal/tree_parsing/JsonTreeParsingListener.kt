package com.topdown.parsers.lang.universal.tree_parsing

class JsonTreeParsingListener(
    contentLength: Long
) : TreeParsingListener(
    GlobalTreeParsingTerminalRules.jsonRules,
    contentLength,
)
