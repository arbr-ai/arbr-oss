package com.topdown.parsers.lang.universal.tree_parsing

class CssTreeParsingListener(
    contentLength: Long
): TreeParsingListener(
    GlobalTreeParsingTerminalRules.cssRules,
    contentLength,
)
