package com.topdown.parsers.lang.universal.tree_parsing

import org.antlr.v4.runtime.ParserRuleContext


class UniversalTreeParsingListener(
    contentLength: Long,
) : TreeParsingListener(GlobalTreeParsingTerminalRules.allRules, contentLength) {

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> hardClass(elt: T): Class<T> = elt::class.java as Class<T>

    override fun <T : ParserRuleContext> getTerminalRule(ctx: T): TreeParsingTerminalRule<*> {
        val customRule = super.getTerminalRule(ctx)
        if (customRule != null) {
            return customRule
        }

        val ruleClass = hardClass(ctx)
        val humanRuleName = humanContextName(ctx)
        return TreeParsingTerminalRule(
            ruleClass,
            humanRuleName,
        ) {
            // Seems essentially impossible to extract a name in the sense we want in a universal way
            // Could just have lots of class matching
            humanRuleName
        }
    }
}
