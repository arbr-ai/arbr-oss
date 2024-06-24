package com.topdown.parsers.lang.universal.tree_parsing

import com.topdown.parsers.lang.jsx.base.JavaScriptParser
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

class JavaScriptTreeParsingListener(
    contentLength: Long,
    tokens: CommonTokenStream,
) : TreeParsingListener(
    GlobalTreeParsingTerminalRules.javaScriptRules,
    contentLength,
)
