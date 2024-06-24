package com.topdown.parsers.lang.universal.tree_parsing

import org.antlr.v4.runtime.ParserRuleContext

data class TreeParsingTerminalRule<T : ParserRuleContext>(
    val cls: Class<T>,
    /**
     * Human readable rule name
     */
    val humanRuleName: String,
    /**
     * Identifier name
     */
    val getName: (T) -> String?,
)