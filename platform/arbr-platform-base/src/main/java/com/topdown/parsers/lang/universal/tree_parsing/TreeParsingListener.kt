package com.topdown.parsers.lang.universal.tree_parsing

import org.antlr.v4.runtime.ParserRuleContext


open class TreeParsingListener(
    terminalRules: List<TreeParsingTerminalRule<*>>,
    contentLength: Long,
) : BaseTreeParsingListener(contentLength) {
    private val terminalRuleMap = terminalRules.associateBy { it.cls }

    override fun <T : ParserRuleContext> getTerminalRule(ctx: T): TreeParsingTerminalRule<*>? {
        return terminalRuleMap[ctx::class.java]
    }
}
