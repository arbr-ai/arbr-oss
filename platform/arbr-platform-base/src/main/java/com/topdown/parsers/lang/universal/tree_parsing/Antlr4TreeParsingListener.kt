package com.topdown.parsers.lang.universal.tree_parsing

class Antlr4TreeParsingListener(
    contentLength: Long
): TreeParsingListener(
    GlobalTreeParsingTerminalRules.allRules, // TODO
    contentLength,
)